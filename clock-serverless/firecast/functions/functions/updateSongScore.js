const db = require("./init");
const functions = require("firebase-functions");
const getSong = require("./Utils/getSong");
const updateUserSongHistory = require("./updateUserSongHistory");

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

  historySongLog.isLiked = isLiked;
  historySongLog.isRanked = "1";
  return { historySongLog, historySongId };
}

const delayParam = 51 * 1000;

async function updateSongScore(data, context) {
  var logMsg = {};
  try {
    let updates = {};
    let update = async () => {
      updates["songs/" + data.songId] = await updateSong(data, context);
      //update user's history
      let { historySongLog, historySongId } = await updateUserLog(
        data,
        context
      );
      updates[
        "users/" + context.auth.uid + "/history/" + historySongId
      ] = historySongLog;
    };
    try {
      await update();
    } catch (err) {
      try {
        const sleep = m => new Promise(r => setTimeout(r, m));
        await sleep(delayParam);
        await update();
      } catch (err) {
        data["sec"] = "10";
        await updateUserSongHistory(data, context);
        await update();
      }
    }
    await db.ref().update(updates);
    logMsg = updates;
  } catch (err) {
    logMsg["error"] = err + " uid= " + context.auth.uid;
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
