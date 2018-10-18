const db = require("./init");
const functions = require("firebase-functions");
const getSong = require("./Utils/getSong");
const chooseIndex = require("./Algorithms/Randomise");
const rootGroupId = "-1";

async function getNewSongUrl(user) {
  let found = false;
  let lastSongIndex = 0;
  let size;
  let nextGroup;
  let filteredNextGroup;
  try {
    while (!found) {
      let lastLikedSong = await getLastLikedSong(user, lastSongIndex++);
      let currGroup =
        lastLikedSong == null ? rootGroupId : lastLikedSong.songId;
      let lastChance = currGroup === rootGroupId;
      let nextGroup = await getNextGroup(currGroup);
      filteredNextGroup = await filterGroup(nextGroup, user, lastChance);
      size = Object.keys(filteredNextGroup).length;
      found = size > 0 ? true : false;
    }

    let nextSongId = chooseIndex(filteredNextGroup);
    let nextSongUrl = filteredNextGroup[nextSongId].url;
    let nexSongTitle =
      filteredNextGroup[nextSongId].artist +
      " - " +
      filteredNextGroup[nextSongId].name;
    let res = {
      url: nextSongUrl,
      songId: nextSongId,
      title: nexSongTitle
    };
    return res;
  } catch (err) {
    throw new Error(err);
  }
}

async function filterGroup(group, user, lastChance) {
  let songHistoryQuery = db
    .ref("/users")
    .child(user)
    .child("history");
  let snapshot = await songHistoryQuery.once("value");
  let historyGroup = snapshot.val();

  let keeper1 = Object.assign({}, group);
  for (let song in historyGroup) {
    if (group[historyGroup[song].songId] != undefined) {
      delete group[historyGroup[song].songId];
    }
  }
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

async function getNextGroup(lastSongId) {
  //first tries to get the group that the current song brought to the db
  //(song's children in tree). we assume it's not a leaf.
  let nextGroupID = lastSongId;
  let matchGroupQuery = db
    .ref("songs")
    .orderByChild("groupId")
    .equalTo(nextGroupID);

  try {
    let snapshot = await matchGroupQuery.once("value");
    let arrGroup = snapshot.val();
    if (Array.isArray(arrGroup)) {
      arrGroup = Object.assign({}, arrGroup);
    }
    //checkig if it's a leaf-no children
    if (arrGroup == null) {
      throw new Error("Leaf Exception");
    } else {
      return arrGroup;
    }
  } catch (err) {
    let prevGroup = await getSongGroupId(lastSongId);
    let res = await getNextGroup(prevGroup);
    return res;
  }
}

async function getSongGroupId(songId) {
  let res = await getSong(songId);
  return res.groupId;
}

async function getLastLikedSong(user, index) {
  var likeSongsQuery = db
    .ref("/users")
    .child(user)
    .child("history")
    .orderByChild("isLiked")
    .equalTo("1");
  try {
    let snapshot = await likeSongsQuery.once("value");
    var history = Object.values(snapshot.val());
    history
      .sort(function(x, y) {
        return x.timestamp - y.timestamp;
      })
      .reverse();
    if (index == history.length) {
      throw new Error("No more history");
    }
    return history[index];
  } catch (err) {
    newGroupId = null;
    return newGroupId;
  }
}

async function getSongUrl(data, context) {
  var logMsg;
  try {
    let user = context.auth.uid;
    let res = await getNewSongUrl(user);

    logMsg = res;
    logMsg.User = user;
    logMsg.timestamp = Date.now();

    return res;
  } catch (err) {
    logMsg = err;
    throw new functions.https.HttpsError("Failed to update song score", err);
  } finally {
    console.log(logMsg);
  }
}

module.exports = getSongUrl;
