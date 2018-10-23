const db = require("./init");
const functions = require("firebase-functions");
const getSong = require("./Utils/getSong");

async function updateSong(data, context) {
  const songId = data.songId;
  let uid = context.auth.uid;
  let isLiked = data.isLiked;
  //update the song's data
  let currSongQuery = db.ref("songs/" + songId);
  let currSong = await getSong(songId);
  let newScore = isLiked == 1 ? currSong.score + 1 : currSong.score;
  let incCount = ++currSong.count;
  currSong.score = newScore;
  currSong.count = incCount;
  return currSong;
}

async function updateUserLog(data, context) {
  const songId = data.songId;
  let uid = context.auth.uid;
  let isLiked = data.isLiked;
  const getSongHistoryIdQuery = db
    .ref("users/" + uid)
    .child("history")
    .orderByChild("songId")
    .equalTo(songId);
  let snapshot = await getSongHistoryIdQuery.once("value");
  let historySongLog = Object.values(snapshot.val())[0];
  let historySongId = Object.keys(snapshot.val())[0];

  historySongLog.isLiked = isLiked;
  historySongLog.isRanked = "1";
  return { historySongLog, historySongId };
}

async function updateSongScore(data, context) {
  var logMsg = {} ;
  try {
    let updates = {};
    updates["songs/" + data.songId] = await updateSong(data, context);
    //update user's history
    let {historySongLog,historySongId}= await updateUserLog(data, context);
    updates["users/" + context.auth.uid + "/history/" + historySongId] = historySongLog;

    await db.ref().update(updates);
    logMsg = updates;
  } catch (err) {
    logMsg["error"] = err +" uid= " + context.auth.uid;
    throw new functions.https.HttpsError("Failed to update song score ", logMsg);
  } finally {
    console.log(logMsg);
  }
}

module.exports = updateSongScore;
