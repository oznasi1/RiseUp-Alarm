//Author: Alex Perry
//Algorithm:
//get: object of songs
//returns: the index of the song with
// greater avg= score/count

function Algorithm(group) {
  let size = Object.keys(group).length;
  let sorted = Object.keys(group).sort((a, b) => {
    if (a.count > 0 && b.count > 0) {
      return a.score / a.count - b.score / b.count;
    }
    if (a.count == 0) return 1;
    if (b.count == 0) return -1;
  });

  let nextSongId = sorted.reverse()[0].songId;

  return nextSongId;
}

module.exports = Algorithm;
