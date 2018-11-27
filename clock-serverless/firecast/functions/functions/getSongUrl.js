//Author: Alex Perry
//Date: 16.10.18
//getSongUrl:
// get function: return the next song should be played
// gets: data: none, context: {authenticated uid}
"use strict";
const db = require("./init");
const functions = require("firebase-functions");
const getSong = require("./Utils/getSong");
const chooseIndex = require("./Algorithms/Randomise");
const rootGroupId = "-1";

//this func is the "pipe" for the algorithm. it manages the decisions making
async function getNewSongUrl(user) {
  let size;
  let filteredNextGroup;
  try {
    //get the last like songs by the user
    let lastLikedSongs = await getLastLikedSongs(user);
    //get the sons of each liked song
    let nextGroup = await getNextGroup(lastLikedSongs);
    //check if theres no songs he liked before
    let lastChance = isAllSongsBase(nextGroup);
    //filter the group's songs
    filteredNextGroup = await filterGroup(nextGroup, user, lastChance);
    size = Object.keys(filteredNextGroup).length;
    //in case of no songs in suggestions
    if (size == 0) {
      let getBaseGroup = async () => {
        let baseGroup = await getNextGroup(null);
        return baseGroup;
      };
      nextGroup = await getBaseGroup();
      lastChance = isAllSongsBase(nextGroup);
      filteredNextGroup = await filterGroup(nextGroup, user, lastChance);
    }
    //we use a base song to each list to get a chance of diversity
    filteredNextGroup = await tryAddBaseSongs(user, filteredNextGroup);
    //get the song from the group
    let nextSongId = chooseIndex(filteredNextGroup);
    //assemble the packet
    let nextSongUrl = filteredNextGroup[nextSongId].url;
    let nexSongTitle =
      filteredNextGroup[nextSongId].title != undefined
        ? filteredNextGroup[nextSongId].title
        : filteredNextGroup[nextSongId].artist +
          " - " +
          filteredNextGroup[nextSongId].name;

    let res = {
      url: nextSongUrl,
      songId: nextSongId,
      title: nexSongTitle
    };
    return res;
  } catch (err) {
    throw new Error(err + " userId = " + user);
  }
}

//check if all og songs are base songs
function isAllSongsBase(group) {
  return Object.values(group).every(song => {
    return song.groupId == rootGroupId;
  });
}

//manages the filtering of the songs list
async function filterGroup(group, user, lastChance) {
  try {
    let songHistoryQuery = db
      .ref("/users")
      .child(user)
      .child("history");
    let snapshot = await songHistoryQuery.once("value");
    let historyGroup = snapshot.val();
    let keeper1 = Object.assign({}, group);
    for (let song in historyGroup) {
      let currHistorySongId = historyGroup[song].songId;
      let currHistorySongUrl =
        historyGroup[song].url != null
          ? historyGroup[song].url
          : await getUrl(currHistorySongId);
      let timeNow = Date.now();
      let timeToForget = 1000 * 60 * 60 * 24 * 7 * 3; // second * 60sec * 60minutes* 24Hours * 7Days * 3Weeks
      let isTimePassed = timeNow - historyGroup[song].timestamp > timeToForget;
      for (let songInGroup in group) {
        //check if song already in history
        let isPlayed = (group[currHistorySongId] != undefined);
        let isTimeNotPassed = !isTimePassed;
        //check if the user liked the song
        let isSongNotLiked = !historyGroup[song].isLiked;
        let isSameUrl = (group[songInGroup].url === currHistorySongUrl);
        if ((isPlayed || isSameUrl) && (isTimeNotPassed || isSongNotLiked)) {
          //case of song played and the time passed since or the song was unliked
          delete group[currHistorySongId];
        } 
      }
    }
    //remove same artist from prev alert
    group = await tryRemoveSameArtist(user, group);
    //no songs left get the last liked songs
    if (Object.keys(group).length === 0 && lastChance) {
      group = keeper1;
      let keeper2 = Object.assign({}, group);
      for (let song in historyGroup) {
        if (historyGroup[song].isLiked != "1") {
          delete group[historyGroup[song].songId];
        }
      }

      if (Object.keys(group).length === 0) {
        group = keeper2;
      }
    }

    return group;
  } catch (err) {
    throw err;
  }
}
//adds base song if there aren't any for diversity
async function tryAddBaseSongs(user, group) {
  try {
    let isBaseSongs = Object.keys(group).some(
      song => song.groupId == rootGroupId
    );
    if (!isBaseSongs) {
      let nextGroup = await getNextGroup(null);
      let baseSongsGroup = await filterGroup(nextGroup, user, false);
      if (Object.keys(baseSongsGroup).length <= 0) {
        throw new Error("No base songs left!");
      }
      let nextSong = chooseIndex(baseSongsGroup);
      group[nextSong] = baseSongsGroup[nextSong];
      return group;
    }
  } catch (error) {
    console.error(error + " " + user);
    return group;
  }
}
//removes the songs of the recent song's artist
async function tryRemoveSameArtist(user, group) {
  try {
    const lastLikedSong = await getLastLikedSong(user);
    var resGroup = Object.keys(group).reduce(function(r, e) {
      if (!lastLikedSong.artist.includes(group[e].artist)) r[e] = group[e];
      return r;
    }, {});
    return Object.keys(resGroup).length > 0 ? resGroup : group;
  } catch (err) {
    return group;
  }
}
//return the liked songs from history
async function getLastLikedSong(user) {
  try {
    let historySongs = await getLastLikedSongs(user);
    //historySongs.sort((a, b) => b.timestamp - a.timestamp);
    let lastLiked = await getSong(historySongs[0].songId);
    return lastLiked;
  } catch (err) {
    return null;
  }
}

//returns an object of sons of last liked songs
async function getNextGroup(lastSongs) {
  try {
    let res = {};
    for (let i in lastSongs) {
      let nextGroupId = lastSongs[i].songId;
      res = Object.assign({}, res, await getGroup(nextGroupId));
    }
    if (lastSongs == null || Object.keys(res).length == 0) {
      return getGroup(rootGroupId);
    }
    return res;
  } catch (err) {
    console.error(err);

    return getGroup(rootGroupId);
  }
}
//get all the songs with the group id equals groupId
async function getGroup(groupId) {
  let matchGroupQuery = db
    .ref("songs")
    .orderByChild("groupId")
    .equalTo(groupId);
  let snapshot = await matchGroupQuery.once("value");
  let arrGroup = snapshot.val();
  if (Array.isArray(arrGroup)) {
    arrGroup = Object.assign({}, arrGroup);
  }
  return arrGroup;
}

async function getUrl(songId) {
  let res = await getSong(songId);
  return res.url;
}
//returns an array of the history liked songs
async function getLastLikedSongs(user) {
  var likeSongsQuery = db
    .ref("/users")
    .child(user)
    .child("history")
    .orderByChild("isLiked")
    .equalTo("1");
  try {
    let snapshot = await likeSongsQuery.once("value");
    var history = Object.values(snapshot.val());
    history.sort((a, b) => b.timestamp - a.timestamp);

    return history;
  } catch (err) {
    let newGroupId = null;
    return newGroupId;
  }
}

async function getSongUrl(data, context) {
  var logMsg = {};
  try {
    let user = context.auth.uid;
    if (!user) {
      throw new functions.https.HttpsError("NOT AUTHORIZED", "NOT AUTHORIZED");
    }
    let res = await getNewSongUrl(user);

    logMsg = res;
    logMsg.User = user;
    logMsg.timestamp = Date.now();

    return res;
  } catch (err) {
    logMsg["error"] = err;
    throw new functions.https.HttpsError("Failed to retrive url, ", err);
  } finally {
    console.log(logMsg);
  }
}

module.exports = getSongUrl;
