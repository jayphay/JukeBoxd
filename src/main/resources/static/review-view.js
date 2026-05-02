const escapeHtml = (s) =>
  String(s)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");

const qs = (k) => new URLSearchParams(window.location.search).get(k);

window.addEventListener("DOMContentLoaded", () => {
  const mount = document.getElementById("review-view");
  if (!mount) return;

  const id = qs("id");
  if (!id || !window.JukeboxdReviews) {
    mount.innerHTML = `
      <div class="empty">
        <div class="empty__title">Review not found</div>
        <div class="empty__body">Missing or invalid review id.</div>
        <div class="actions">
          <a class="btn btn--primary" href="/create-review.html">Create a review</a>
          <a class="btn btn--ghost" href="/">Go home</a>
        </div>
      </div>
    `;
    return;
  }

  const r = window.JukeboxdReviews.getReviewById(id);
  if (!r) {
    mount.innerHTML = `
      <div class="empty">
        <div class="empty__title">Review not found</div>
        <div class="empty__body">We couldn’t find that review in this browser.</div>
        <div class="actions">
          <a class="btn btn--primary" href="/create-review.html">Create a review</a>
          <a class="btn btn--ghost" href="/">Go home</a>
        </div>
      </div>
    `;
    return;
  }

  const headline = escapeHtml(r.headline ?? `${r.title ?? ""} — ${r.artist ?? ""}`.trim());
  const author = escapeHtml(r.author ?? "anonymous");
  const rating = escapeHtml(r.rating ?? "");
  const type = escapeHtml(r.type ?? "");
  const body = escapeHtml(r.body ?? "");
  const when = escapeHtml(window.JukeboxdReviews.timeAgo(r.createdAt));

  document.title = `${headline} • Jukeboxd`;

  mount.innerHTML = `
    <div class="reviewPage">
      <div class="reviewPage__head">
        <div>
          <div class="reviewPage__title">${headline}</div>
          <div class="reviewPage__meta">by ${author}${when ? ` • ${when}` : ""}${type ? ` • ${type}` : ""}</div>
        </div>
        ${rating ? `<div class="score">${rating}</div>` : ""}
      </div>
      <div class="reviewPage__body">${body}</div>
      <div class="actions">
        <a class="btn btn--ghost" href="/">Back to home</a>
        <a class="btn btn--primary" href="/create-review.html">Write another</a>
      </div>
    </div>
  `;
});

