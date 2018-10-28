const db = require("./init");
const functions = require("firebase-functions");
const getSong = require("./Utils/getSong");
const chooseIndex = require("./Algorithms/Randomise");
const rootGroupId = "-1";

async function getNewSongUrl(user) {
  let size;
  let filteredNextGroup;
  try {
    let lastLikedSongs = await getLastLikedSongs(user);
    //let currGroup;//TODO
    //lastLikedSongs == null ? rootGroupId : lastLikedSong.songId;
    let nextGroup = await getNextGroup(lastLikedSongs);
    let lastChance = Object.values(nextGroup).every(song => {
      return song.groupId == rootGroupId;
    });
    filteredNextGroup = await filterGroup(nextGroup, user, lastChance);
    size = Object.keys(filteredNextGroup).length;
    if (size == 0) {
      nextGroup = await getNextGroup(null);
      lastChance = Object.values(nextGroup).every(song => {
        return song.groupId == rootGroupId;
      });
      filteredNextGroup = await filterGroup(nextGroup, user, lastChance);
    }
    filteredNextGroup = await tryAddBaseSongs(user, filteredNextGroup);
    let nextSongId = chooseIndex(filteredNextGroup);
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
    throw new Error(err + "userId = " + user);
  }
}

async function filterGroup(group, user, lastChance) {
  let songHistoryQuery = db
    .ref("/users")
    .child(user)
    .child("history");
  let snapshot = await songHistoryQuery.once("value");
  let historyGroup = snapshot.val();
  //remove songs that already palayd
  let keeper1 = Object.assign({}, group);
  for (let song in historyGroup) {
    if (group[historyGroup[song].songId] != undefined) {
      delete group[historyGroup[song].songId];
    }
  }
  //remove same artist from prev alert
  group = await tryRemoveSameArtist(user, group);
  //no songs left left get the last liked songs
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
}
//adds base song if there aren't any for diversity  
async function tryAddBaseSongs(user, group) {
  try {
    let isBaseSongs = Object.keys(group).some((song)=> song.groupId == rootGroupId);
    if(!isBaseSongs){
    let nextGroup = await getNextGroup(null);
    let baseSongsGroup = await filterGroup( nextGroup, user, false);
    let nextSong = chooseIndex(baseSongsGroup);
    group[nextSong] = baseSongsGroup[nextSong];
    return group;
  }
  } catch (error) {

    return group;
  }
}
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

async function getSongGroupId(songId) {
  let res = await getSong(songId);
  return res.groupId;
}

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
    newGroupId = null;
    return newGroupId;
  }
}

async function getSongUrl(data, context) {
  var logMsg = {};
  try {
    let user = context.auth.uid;
    let res = await getNewSongUrl(user);

    logMsg = res;
    logMsg.User = user;
    logMsg.timestamp = Date.now();

    return res;
  } catch (err) {
    logMsg["error"] = err + " uid= " + context.auth.uid;
    throw new functions.https.HttpsError("Failed to retrive url, ", err);
  } finally {
    console.log(logMsg);
  }
}

module.exports = getSongUrl;
