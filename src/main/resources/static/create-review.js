const byId = (id) => document.getElementById(id);

window.addEventListener("DOMContentLoaded", () => {
  const form = byId("review-form");
  if (!form) return;

  form.addEventListener("submit", (e) => {
    e.preventDefault();

    const fd = new FormData(form);
    const type = String(fd.get("type") ?? "").trim();
    const title = String(fd.get("title") ?? "").trim();
    const artist = String(fd.get("artist") ?? "").trim();
    const rating = String(fd.get("rating") ?? "").trim();
    const author = String(fd.get("author") ?? "").trim();
    const body = String(fd.get("body") ?? "").trim();

    if (!window.JukeboxdReviews) {
      alert("Review system failed to load.");
      return;
    }

    const created = window.JukeboxdReviews.addReview({
      type,
      title,
      artist,
      rating,
      author,
      body,
    });

    window.location.href = `/review.html?id=${encodeURIComponent(created.id)}`;
  });
});

