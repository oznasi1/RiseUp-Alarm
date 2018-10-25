const functions = require("firebase-functions");

const getSongUrl = require("./getSongUrl");

const updateSongScore = require("./updateSongScore");

const updateUserSongHistory = require("./updateUserSongHistory");

var user = {
  auth: {
    uid: "iVUcaEHrdVW8iTfNmU4yGUvxbrm1"
  },
  timestamp: 1540358107754
};
var dataHistory = {

};

async function runTest() {
  while (true) {
    let song = await getSongUrl(dataHistory, user);
    let data = {
      sec: 36,
      isRanked: "0",
      songId: song.songId,
      isLiked: "0",
      timestamp: Date.now()
    };
    user["timestamp"] = data.timestamp;

    const fs = require("fs");
    var content= fs.readFileSync("./answer.txt", 'utf8');

    data["isLiked"] = content;

    let resupdateUserSongHistory = await updateUserSongHistory(data, user);

    let resupdateSongScore = await updateSongScore(data, user);
  }
}
runTest();
