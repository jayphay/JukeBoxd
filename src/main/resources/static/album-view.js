/**
 * album-view.js — loads album info and songs.
 */

const getParam = (key) => new URLSearchParams(window.location.search).get(key);

const fetchJson = async (url) => {
    const res = await fetch(url, { headers: { Accept: "application/json" } });
    if (!res.ok) throw new Error(`Failed to load: ${res.status}`);
    return res.json();
};

const renderAlbumInfo = (info) => {
    document.getElementById("album-title").textContent = info.title;
    document.getElementById("album-artist").textContent = info.artistName;
    document.getElementById("album-year").textContent = info.releaseYear || "Unknown Year";
    const ratingEl = document.getElementById("album-rating");
    if (ratingEl) ratingEl.textContent = info.avgRating;
    document.title = `${info.title} • Jukeboxd`;
};

const renderSongs = (songs) => {
    const container = document.getElementById("song-container");
    document.getElementById("track-count").textContent = `${songs.length} Song${songs.length === 1 ? "" : "s"}`;

    if (songs.length === 0) {
        container.innerHTML = `<div style="padding: 24px; color: var(--muted2);">No tracks found.</div>`;
        return;
    }

    container.innerHTML = songs.map((song, index) => {
        // Show rating if it exists, otherwise show a muted dash
        const ratingDisplay = song.avgRating ? `★ ${song.avgRating}` : "—";

        return `
      <div class="song-row">
        <div class="song-row__index">${index + 1}</div>
        <div class="song-row__title">${song.title}</div>
        <div class="song-row__genre">${song.genre || "—"}</div>
        <div class="song-row__rating">${ratingDisplay}</div>
      </div>
    `;
    }).join("");
};

// NEW: Render the album reviews
const renderReviews = (reviews) => {
    const container = document.getElementById("reviews-container");
    if (reviews.length === 0) {
        container.innerHTML = `<div style="padding: 24px; color: var(--muted2);">No reviews yet for this album.</div>`;
        return;
    }

    container.innerHTML = reviews.map(rev => `
    <div class="review" style="padding: 16px 0; border-bottom: 1px solid rgba(255,255,255,0.05);">
      <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 4px;">
        <div>
          <span style="font-weight: 600; color: var(--accent);">@${rev.username}</span>
          <span style="color: var(--muted2); font-size: 13px;"> on ${rev.songTitle}</span>
        </div>
        <div style="color: var(--accent); letter-spacing: 2px;">
          ${"★".repeat(Number(rev.rating))}${"☆".repeat(5 - Number(rev.rating))}
        </div>
      </div>
      <div style="font-size: 14px; color: var(--text); line-height: 1.4;">${rev.comment}</div>
    </div>
  `).join("");
};

window.addEventListener("DOMContentLoaded", async () => {
    const albumId = getParam("id");
    if (!albumId) return;

    try {
        // UPDATED: Now fetching three endpoints
        const [info, songs, reviews] = await Promise.all([
            fetchJson(`/api/albums/${albumId}`),
            fetchJson(`/api/albums/${albumId}/songs`),
            fetchJson(`/api/albums/${albumId}/reviews`)
        ]);

        renderAlbumInfo(info);
        renderSongs(songs);
        renderReviews(reviews);
    } catch (err) {
        console.error("Error loading album data:", err);
    }
});