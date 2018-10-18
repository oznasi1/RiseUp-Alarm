const db = require("../init");
const functions = require("firebase-functions");
async function getSong(songId) {
  let songQuery = db
    .ref("songs")
    .orderByKey()
    .equalTo(songId);

  let res;
  try {
    let snapshot = await songQuery.once("value");
    res = snapshot.val()[songId];

    return res;
  } catch (err) {
    console.error(err);
  }
}

module.exports = getSong;