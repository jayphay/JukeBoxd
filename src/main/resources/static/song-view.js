const getParam = (key) => new URLSearchParams(window.location.search).get(key);

const fetchJson = async (url) => {
  const res = await fetch(url, { headers: { Accept: "application/json" } });
  if (!res.ok) throw new Error("Failed to load");
  return res.json();
};

const renderSong = (data) => {
  document.getElementById("song-title").textContent = data.title;
  document.getElementById("song-artist").textContent = data.artistName;
  document.getElementById("song-genre").textContent = data.genre || "Unknown Genre";
  document.getElementById("avg-rating").textContent = data.avgRating;
  document.getElementById("write-review").textContent = "Write a Review";
  document.getElementById("write-review").href = `/create-review`;
  
  const albumLink = document.getElementById("album-link");
  if (data.albumId) {
    albumLink.textContent = data.albumTitle;
    albumLink.href = `/album?id=${data.albumId}`;
  } else {
    albumLink.textContent = "Single";
    albumLink.style.pointerEvents = "none";
  }
  document.title = `${data.title} • Jukeboxd`;
};

const renderReviews = (reviews) => {
  const container = document.getElementById("reviews-container");
  if (!reviews.length) {
    container.innerHTML = `<div style="padding:20px; color:var(--muted2)">No reviews yet.</div>`;
    return;
  }
  container.innerHTML = reviews.map(r => `
    <div class="review" style="padding:16px 0; border-bottom:1px solid rgba(255,255,255,0.05)">
      <div style="display:flex; justify-content:space-between;">
        <span style="color:var(--accent); font-weight:600;">@${r.username}</span>
        <span>${"★".repeat(r.rating)}${"☆".repeat(5-r.rating)}</span>
      </div>
      <p style="margin:8px 0 0; font-size:14px;">${r.comment}</p>
    </div>
  `).join("");
};

window.addEventListener("DOMContentLoaded", async () => {
  const id = getParam("id");
  if (!id) return;

  try {
    const [song, reviews] = await Promise.all([
      fetchJson(`/api/songs/${id}`),
      fetchJson(`/api/songs/${id}/reviews`)
    ]);
    renderSong(song);
    renderReviews(reviews);
  } catch (e) {
    document.getElementById("song-title").textContent = "Song not found";
  }
});