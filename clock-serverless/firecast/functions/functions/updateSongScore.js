//Author: Alex Perry
//Date: 16.10.18
//updateSongScore:
// get function: updates the db for users activity after the user voted un/like
// gets: data: {isLiked}, context: {auth.uid}
'use strict';
const db = require("./init");
const functions = require("firebase-functions");
const getSong = require("./Utils/getSong");
const updateUserSongHistory = require("./updateUserSongHistory");

//updating the song/songId 
async function updateSong(data, context) {
  const songId = data.songId;
  let isLiked = data.isLiked;
  //update the song's data
  let currSong = await getSong(songId);
  let newScore = isLiked == 1 ? currSong.score + 1 : currSong.score;
  let incCount = ++currSong.count;
  currSong.score = newScore;
  currSong.count = incCount;

  return currSong;
}

//updating the userId/history/currLog
async function updateUserLog(data, context) {
  const songId = data.songId;
  let uid = context.auth.uid;
  let isLiked = data.isLiked;
  const getSongHistoryIdQuery = db
    .ref("users/" + uid + "/history")
    .orderByChild("songId")
    .equalTo(songId);
  let snapshot = await getSongHistoryIdQuery.once("value");
  let historySongLog = Object.values(snapshot.val())[0];
  let historySongId = Object.keys(snapshot.val())[0];
  let song = await getSong(data.songId); 

  historySongLog.isLiked = isLiked;
  historySongLog.isRanked = "1";
  historySongLog["url"] = song.url
  return { historySongLog, historySongId };
}

//delay that helps for fault tolerance
const delayParam = 51 * 1000;
const timePlayedBackup = "10";
async function updateSongScore(data, context) {
  var logMsg = {};
  try {
    if(data.songId == null){
      throw new Error("songId_NullException");
    }
    let user = context.auth.uid;
    if(!user){
      throw new functions.https.HttpsError(
        "NOT AUTHORIZED",
        "NOT AUTHORIZED"
      );
    }
    let updates = {};
    let update = async () => {
      updates["songs/" + data.songId] = await updateSong(data, context);
      //update user's history
      let { historySongLog, historySongId } = await updateUserLog(
        data,
        context
      );
      updates[
        "users/" + user + "/history/" + historySongId
      ] = historySongLog;
    };
    try {
      await update();
    } catch (err) {
      try {
        //delays and invokes again
        const sleep = m => new Promise(r => setTimeout(r, m));
        await sleep(delayParam);
        await update();
      } catch (err) {
        //case of lost/delayed packet, invoke the prev synch service here
        data["sec"] = timePlayedBackup;
        await updateUserSongHistory(data, context);
        await update();
      }
    }
    await db.ref().update(updates);
    logMsg = updates;
  } catch (err) {
    logMsg["error"] = err + " uid: " + user;
    logMsg["reqData"] = data;
    throw new functions.https.HttpsError(
      "Failed to update song score ",
      logMsg
    );
  } finally {
    console.log(logMsg);
  }
}

module.exports = updateSongScore;
