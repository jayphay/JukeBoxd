const escapeHtml = (s) =>
  String(s)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");

const renderRows = (el, items, kindLabel) => {
  if (!el) return;
  el.innerHTML = items.map((it) => {
    const isSong = kindLabel === "Single" || kindLabel === "Result" || kindLabel === "Search Result";
    const savedClass = it.isSaved ? 'is-saved' : '';
    const btnText = it.isSaved ? 'Saved' : '+ List'; // Shorter text

    return `
      <li class="row" style="display: flex; justify-content: space-between; align-items: center;">
        <a href="${isSong ? `/song?id=${encodeURIComponent(it.songId)}` : `/album?id=${encodeURIComponent(it.albumId)}`}" class="row__left" style="text-decoration: none; color: inherit;">
          <div class="row__left">
            <div class="row__title">${escapeHtml(it.title)}</div>
            <div class="row__sub">${escapeHtml(it.subtitle ?? "")}</div>
          </div>
          <div class="row__right">
            ${isSong ? `
              <button class="listen-toggle ${savedClass}" data-id="${it.songId}">
                ${btnText}
              </button>
            ` : ""}
          </div>
        </a>
      </li>
    `;
  }).join("");
};
document.addEventListener('click', async (e) => {
  const btn = e.target.closest('.listen-toggle');
  if (!btn) return;

  e.preventDefault();
  const songId = btn.getAttribute('data-id');
  const isListenListPage = btn.getAttribute('data-page') === 'listenlist';

  try {
    const res = await fetch(`/api/home/listen-list/toggle?songId=${encodeURIComponent(songId)}`, {
      method: 'POST'
    });

    if (res.ok) {
      const data = await res.json();

      if (data.status === 'removed' && isListenListPage) {
        // OPTION A: Remove the entire row from the list immediately
        btn.closest('.row').style.opacity = '0';
        setTimeout(() => btn.closest('.row').remove(), 300);
      } else {
        // OPTION B: Just toggle the button (for search/home pages)
        const isAdded = data.status === 'added';
        btn.innerText = isAdded ? 'SAVED' : '+ LIST';
        btn.classList.toggle('is-saved', isAdded);
      }
    }
  } catch (err) {
    console.error("Error:", err);
  }
});

const renderChips = (el, items) => {
  el.innerHTML = items
    .map((it) => {
      const name = escapeHtml(it.name);
      const meta = escapeHtml(it.meta ?? "");
      return `<div class="chip"><strong>${name}</strong>${meta ? `<span>${meta}</span>` : ""}</div>`;
    })
    .join("");
};

const renderReviews = (el, items) => {
  el.innerHTML = items
    .map((it) => {
      const headline = escapeHtml(it.headline);
      const meta = escapeHtml(it.meta ?? "");
      const body = escapeHtml(it.body ?? "");
      const rating = escapeHtml(it.rating ?? "");
      const id = escapeHtml(it.id ?? "");
      return `
        <div class="review">
          <div class="review__main">
            <div class="review__headline">
              ${id ? `<a class="link" href="/review.html?id=${id}">${headline}</a>` : headline}
            </div>
            ${meta ? `<div class="review__meta">${meta}</div>` : ""}
            ${body ? `<div class="review__body">${body}</div>` : ""}
          </div>
          ${rating ? `<div class="score">${rating}</div>` : ""}
        </div>
      `;
    })
    .join("");
};

const toHomeReview = (r) => {
  const when = window.JukeboxdReviews?.timeAgo?.(r.createdAt) ?? "";
  const meta = `by ${r.author ?? "anonymous"}${when ? ` • ${when}` : ""}`;
  return {
    id: r.id,
    headline: r.headline ?? `${r.title ?? ""} — ${r.artist ?? ""}`.trim(),
    meta,
    body: r.body ?? "",
    rating: r.rating ?? "",
  };
};

const fetchJson = async (url) => {
  const res = await fetch(url, { headers: { Accept: "application/json" } });
  if (!res.ok) throw new Error(`Request failed: ${res.status}`);
  return await res.json();
};

const ratingStars = (s) => {
  const n = Number(s);
  if (!Number.isFinite(n)) return "";
  if (n <= 1) return "★☆☆☆☆";
  if (n === 2) return "★★☆☆☆";
  if (n === 3) return "★★★☆☆";
  if (n === 4) return "★★★★☆";
  return "★★★★★";
};

// Starter content (replace with real API calls when endpoints exist)
const popularAlbums = [
  { title: "Blonde", subtitle: "Frank Ocean", score: "4.6" },
  { title: "To Pimp a Butterfly", subtitle: "Kendrick Lamar", score: "4.7" },
  { title: "Rumours", subtitle: "Fleetwood Mac", score: "4.5" },
  { title: "Discovery", subtitle: "Daft Punk", score: "4.4" },
  { title: "SOS", subtitle: "SZA", score: "4.2" },
];

const popularSingles = [
  { songId: "1", title: "Bad Habit", subtitle: "Steve Lacy", score: "4.1" },
  { songId: "2", title: "Kill Bill", subtitle: "SZA", score: "4.2" },
  { songId: "3", title: "As It Was", subtitle: "Harry Styles", score: "3.9" },
  { songId: "4", title: "Cruel Summer", subtitle: "Taylor Swift", score: "4.0" },
  { songId: "5", title: "Paint The Town Red", subtitle: "Doja Cat", score: "3.8" },
];

const popularArtists = [
  { name: "Kendrick Lamar", meta: "Albums up" },
  { name: "Taylor Swift", meta: "Reviews up" },
  { name: "Bad Bunny", meta: "Trending" },
  { name: "Beyoncé", meta: "Fan favorite" },
  { name: "Radiohead", meta: "All-time" },
  { name: "The Weeknd", meta: "Singles up" },
  { name: "SZA", meta: "Hot" },
];

const recentReviews = [
  {
    headline: "Discovery — Daft Punk",
    meta: "by alex • 2 hours ago",
    body: "Still feels futuristic — every hook lands.",
    rating: "★★★★★",
  },
  {
    headline: "SOS — SZA",
    meta: "by priya • yesterday",
    body: "Great range and replay value; a few tracks drag.",
    rating: "★★★★☆",
  },
  {
    headline: "Rumours — Fleetwood Mac",
    meta: "by sam • 3 days ago",
    body: "Impossibly tight songwriting. No skips.",
    rating: "★★★★★",
  },
];

window.addEventListener("DOMContentLoaded", () => {
  const albumsEl = document.getElementById("popular-albums");
  const singlesEl = document.getElementById("popular-singles");
  const artistsEl = document.getElementById("popular-artists");
  const reviewsEl = document.getElementById("recent-reviews");

  if (albumsEl) renderRows(albumsEl, popularAlbums, "Album");
  if (singlesEl) renderRows(singlesEl, popularSingles, "Single");
  if (artistsEl) renderChips(artistsEl, popularArtists);
  if (reviewsEl) renderReviews(reviewsEl, recentReviews);

  // Try DB-backed endpoints (populated by populate_db.py). If they fail, keep filler.
  (async () => {
    try {
      if (albumsEl) {
        const rows = await fetchJson("/api/home/popular-albums");
        const items = rows.map((r) => ({
          albumId: r.albumId,
          title: r.title || "(Untitled album)",
          subtitle: `${r.artistName}${r.releaseYear ? ` • ${r.releaseYear}` : ""}`,
          score: r.songsCount ? `${r.songsCount} songs` : "",
        }));
        renderRows(albumsEl, items, "Album");
      }

      if (singlesEl) {
        const rows = await fetchJson("/api/home/popular-singles");
        const items = rows.map((r) => ({
          songId: r.songId,
          title: r.title,
          artistName: r.artistName,
          genre: r.genre,
          isSaved: r.isSaved // <--- Pass the boolean from Java to JS here
        }));
        renderRows(singlesEl, items, "Single");
      }

      if (artistsEl) {
        const rows = await fetchJson("/api/home/popular-artists");
        const items = rows.map((r) => ({
          name: r.artistName,
          meta: `${r.songsCount} songs`,
        }));
        renderChips(artistsEl, items);
      }

      if (reviewsEl) {
        const rows = await fetchJson("/api/home/recent-reviews");
        if (Array.isArray(rows) && rows.length) {
          const items = rows.map((r) => ({
            headline: `${r.songTitle} — ${r.artistName}`,
            meta: `by ${r.username}`,
            body: r.comment,
            rating: ratingStars(r.rating),
          }));
          renderReviews(reviewsEl, items);
        } else {
          // if DB has no reviews yet, fall back to locally-created ones if present
          const saved =
            window.JukeboxdReviews?.loadReviews?.()
              ?.slice(0, 5)
              ?.map(toHomeReview) ?? [];
          if (saved.length) renderReviews(reviewsEl, saved);
        }
      }
    } catch {
      // ignore and keep the starter content
    }
  })();
});

