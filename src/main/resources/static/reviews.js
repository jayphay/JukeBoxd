(() => {
  const REVIEWS_STORAGE_KEY = "jukeboxd.reviews.v1";

  const safeJsonParse = (s, fallback) => {
    try {
      return JSON.parse(s);
    } catch {
      return fallback;
    }
  };

  const nowIso = () => new Date().toISOString();

  const newId = () =>
    typeof crypto !== "undefined" && crypto.randomUUID
      ? crypto.randomUUID()
      : `r_${Date.now()}_${Math.random().toString(16).slice(2)}`;

  const loadReviews = () => {
    const raw = localStorage.getItem(REVIEWS_STORAGE_KEY);
    const parsed = safeJsonParse(raw ?? "[]", []);
    return Array.isArray(parsed) ? parsed : [];
  };

  const saveReviews = (reviews) => {
    localStorage.setItem(REVIEWS_STORAGE_KEY, JSON.stringify(reviews));
  };

  const addReview = ({ type, title, artist, rating, author, body }) => {
    const reviews = loadReviews();
    const id = newId();
    const createdAt = nowIso();
    const headline = `${title} — ${artist}`;

    const review = {
      id,
      type,
      title,
      artist,
      rating,
      author,
      body,
      headline,
      createdAt,
    };

    reviews.unshift(review);
    saveReviews(reviews);
    return review;
  };

  const getReviewById = (id) => {
    const reviews = loadReviews();
    return reviews.find((r) => String(r.id) === String(id)) ?? null;
  };

  const timeAgo = (iso) => {
    const t = new Date(iso).getTime();
    if (!Number.isFinite(t)) return "";
    const diffSec = Math.max(0, Math.floor((Date.now() - t) / 1000));

    const mins = Math.floor(diffSec / 60);
    const hrs = Math.floor(diffSec / 3600);
    const days = Math.floor(diffSec / 86400);

    if (diffSec < 60) return "just now";
    if (mins < 60) return `${mins} min${mins === 1 ? "" : "s"} ago`;
    if (hrs < 24) return `${hrs} hour${hrs === 1 ? "" : "s"} ago`;
    return `${days} day${days === 1 ? "" : "s"} ago`;
  };

  window.JukeboxdReviews = {
    loadReviews,
    saveReviews,
    addReview,
    getReviewById,
    timeAgo,
  };
})();

