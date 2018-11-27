//Author: Alex Perry
//Date: 16.10.18
//updateUserSongHistory:
// post function: updates the db for users activity after he played the song
// and before he un/liked it
// gets: data:{seconds played, songId} context: {uid}
'use strict';

const db = require("./init");
const functions = require("firebase-functions");

async function updateUserSongHistory(data, context) {
  var logMsg = {};
  try {
    if (data.songId == null) {
      throw new Error("songId_NullException");
    }
    let uid = context.auth.uid;
    if(!uid){
      throw new functions.https.HttpsError(
        "NOT AUTHORIZED",
        "NOT AUTHORIZED"
      );
    }
    const update = {
      secondsPlayed: data.sec,
      isRanked: "0",
      songId: data.songId,
      isLiked: "0",
      timestamp: Date.now()
    };
    const query = db.ref("users/" + uid).child("history");

    logMsg["update"] = update;
    logMsg["userId"] = uid;
    await query.push(update);
  } catch (err) {
    logMsg["error"] = err + " uid: " + context.auth.uid;
    throw new functions.https.HttpsError("Failed to update history ", logMsg);
  } finally {
    console.log(logMsg);
  }
}

module.exports = updateUserSongHistory;
