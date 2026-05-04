/**
 * profile.js — loads and renders the user profile page.
 *
 * Resolution order for the username:
 *   1. ?user=<username> query-param  (e.g. /profile.html?user=alex)
 *   2. sessionStorage key "jukeboxd.currentUser"  (set by your login flow)
 *   3. Show the not-found banner if neither is available.
 */

const escapeHtml = (s) =>
  String(s ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");

const qs = (k) => new URLSearchParams(window.location.search).get(k);

const fetchJson = async (url) => {
  const res = await fetch(url, { headers: { Accept: "application/json" } });
  if (!res.ok) throw new Error(`Request failed: ${res.status}`);
  return res.json();
};

/** Convert a numeric rating string ("1"–"5") to star glyphs. */
const ratingStars = (s) => {
  const n = Number(s);
  if (!Number.isFinite(n)) return s ?? "";
  return "★".repeat(n) + "☆".repeat(Math.max(0, 5 - n));
};

/** Derive initials from a full name for the avatar circle. */
const initials = (firstName, lastName) => {
  const f = (firstName ?? "").trim()[0] ?? "";
  const l = (lastName ?? "").trim()[0] ?? "";
  return (f + l).toUpperCase() || "?";
};

/** Render an array of review objects into #user-reviews. */
const renderReviews = (reviews, isOwnProfile = false) => {
  const el = document.getElementById("user-reviews");
  if (!el) return;

  if (!reviews.length) {
    el.innerHTML = `
      <div class="profile-empty">
        <div class="profile-empty__title">No reviews yet</div>
        <div class="profile-empty__body">Reviews written by this user will appear here.</div>
      </div>`;
    return;
  }

  el.innerHTML = reviews
    .map((r) => {
      const headline = escapeHtml(
        r.songTitle && r.artistName
          ? `${r.songTitle} — ${r.artistName}`
          : r.headline ?? ""
      );
      const stars = escapeHtml(ratingStars(r.rating));
      const comment = escapeHtml(r.comment ?? r.body ?? "");
      const songId = escapeHtml(r.songId ?? "");

      const editBtn = isOwnProfile && songId ? `
        <button class="btn btn--tiny btn--ghost" onclick="openEditForm('${songId}', this)">Edit</button>` : "";

      const editForm = isOwnProfile && songId ? `
        <div class="review-edit-form" id="edit-form-${songId}" style="display:none; margin-top:12px;">
          <select class="field__input" id="edit-rating-${songId}" style="margin-bottom:8px; width:100%;">
            <option value="5" ${r.rating==="5"?"selected":""}>★★★★★</option>
            <option value="4" ${r.rating==="4"?"selected":""}>★★★★☆</option>
            <option value="3" ${r.rating==="3"?"selected":""}>★★★☆☆</option>
            <option value="2" ${r.rating==="2"?"selected":""}>★★☆☆☆</option>
            <option value="1" ${r.rating==="1"?"selected":""}>★☆☆☆☆</option>
          </select>
          <textarea class="field__input field__textarea" id="edit-comment-${songId}" rows="3" style="margin-bottom:8px; width:100%;">${comment}</textarea>
          <div class="actions">
            <button class="btn btn--primary btn--tiny" onclick="saveEdit('${songId}')">Save</button>
            <button class="btn btn--ghost btn--tiny" onclick="cancelEdit('${songId}')">Cancel</button>
          </div>
        </div>` : "";

      return `
        <div class="review" id="review-${songId}">
          <div class="review__main">
            <div class="review__headline" style="display:flex; justify-content:space-between; align-items:center;">
              <a href="/song?id=${songId}" style="color: inherit; text-decoration: none;">
                <span>${headline}</span>
              </a>
              ${editBtn}
            </div>
            <div class="review__body" id="review-comment-${songId}">${comment}</div>
            ${editForm}
          </div>
          <div class="score" id="review-stars-${songId}">${stars}</div>
        </div>`;
    })
    .join("");
};

const openEditForm = (songId, btn) => {
  document.getElementById(`edit-form-${songId}`).style.display = "block";
  btn.style.display = "none";
};

const cancelEdit = (songId) => {
  document.getElementById(`edit-form-${songId}`).style.display = "none";
  const btn = document.querySelector(`#review-${songId} .btn--ghost[onclick*="openEditForm"]`);
  if (btn) btn.style.display = "";
};

const saveEdit = async (songId) => {
  const comment = document.getElementById(`edit-comment-${songId}`).value.trim();
  const rating  = document.getElementById(`edit-rating-${songId}`).value;
  if (!comment) return;

  try {
    const res = await fetch(`/api/profile/reviews/${encodeURIComponent(songId)}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ comment, rating }),
    });

    if (res.ok) {
      document.getElementById(`review-comment-${songId}`).textContent = comment;
      document.getElementById(`review-stars-${songId}`).textContent = ratingStars(rating);
      cancelEdit(songId);
    } else {
      alert("Failed to save. Please try again.");
    }
  } catch {
    alert("Something went wrong. Please try again.");
  }
};

/** Try to fetch the user's DB-backed profile from the API. */
const loadProfileFromApi = async (username) => {
  const [userInfo, reviews] = await Promise.all([
    fetchJson(`/api/profile/${encodeURIComponent(username)}`),
    fetchJson(`/api/profile/${encodeURIComponent(username)}/reviews`),
  ]);
  return { userInfo, reviews };
};

/**
 * Build a lightweight profile from localStorage reviews as a fallback
 * when no backend is available yet.
 */
const buildLocalProfile = (username) => {
  const all = window.JukeboxdReviews?.loadReviews?.() ?? [];
  const mine = all.filter(
    (r) => (r.author ?? "").toLowerCase() === username.toLowerCase()
  );

  const avgRating =
    mine.length === 0
      ? null
      : mine.reduce((sum, r) => {
          const n = Number((r.rating ?? "").replace(/[^1-5]/g, ""));
          return sum + (Number.isFinite(n) ? n : 0);
        }, 0) / mine.length;

  return {
    userInfo: {
      username,
      firstName: username,
      lastName: "",
      reviewCount: mine.length,
      listenListCount: 0,
      avgRating: avgRating ? avgRating.toFixed(1) : null,
    },
    reviews: mine,
  };
};

/** Populate all DOM elements with resolved profile data. */
const renderProfile = ({ userInfo, reviews }) => {
  const { username, firstName, lastName, reviewCount, listenListCount, avgRating } =
    userInfo;

  // Avatar
  const avatarEl = document.getElementById("profile-avatar");
  if (avatarEl) avatarEl.textContent = initials(firstName, lastName);

  // Name & username
  const nameEl = document.getElementById("profile-name");
  if (nameEl)
    nameEl.textContent =
      [firstName, lastName].filter(Boolean).join(" ") || username;

  const usernameEl = document.getElementById("profile-username");
  if (usernameEl) usernameEl.textContent = `@${username}`;

  // Stats
  const revCount = reviewCount ?? reviews.length ?? 0;
  document.getElementById("stat-reviews").textContent = revCount;
  document.getElementById("stat-avg").textContent = avgRating
    ? Number(avgRating).toFixed(1)
    : "—";
  document.getElementById("stat-listen").textContent = listenListCount ?? "—";

  // Review count pill
  const pill = document.getElementById("review-count-pill");
  if (pill) pill.textContent = revCount;

  // Page title
  document.title = `${firstName || username} • Jukeboxd`;

  // Reviews list — pass true if this is the logged-in user's own profile
  const isOwnProfile = window.JUKEBOXD_USERNAME === username;
  renderReviews(reviews, isOwnProfile);

};

window.addEventListener("DOMContentLoaded", async () => {
  // 1. Resolve username
  // window.JUKEBOXD_USERNAME is injected by the Thymeleaf template (server-side session)
  const username = window.JUKEBOXD_USERNAME ?? qs("user") ?? null;

  if (!username) {
    return;
  }

  try {
    // 2a. Try real API first
    const data = await loadProfileFromApi(username);
    renderProfile(data);
  } catch {
    // 2b. Fall back to localStorage-only data (useful during early development)
    const data = buildLocalProfile(username);
    renderProfile(data);
  }
});