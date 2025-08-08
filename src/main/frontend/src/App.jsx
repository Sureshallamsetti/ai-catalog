import React, { useEffect, useState } from "react";

export default function App() {
  const [products, setProducts] = useState([]);
  const [results, setResults] = useState([]);
  const [query, setQuery] = useState("");
  const [category, setCategory] = useState("All");
  const [maxPrice, setMaxPrice] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    setLoading(true);
    fetch("/api/products")
      .then((r) => r.json())
      .then((data) => {
        setProducts(data);
        setResults(data);
      })
      .catch(() => setError("Failed to load products."))
      .finally(() => setLoading(false));
  }, []);

  const categories = ["All", ...Array.from(new Set(products.map((p) => p.category)))];

  function applyFilter() {
    setError("");
    const filtered = products.filter(
      (p) =>
        (category === "All" || p.category === category) &&
        (maxPrice === "" || p.price <= parseFloat(maxPrice))
    );
    setResults(filtered);
    if (filtered.length === 0) setError("No products match the filter criteria.");
  }

  async function aiSearch(e) {
    e && e.preventDefault();
    if (!query.trim()) {
      setError("Please enter a search term");
      return;
    }

    setError("");
    setLoading(true);
    try {
      const resp = await fetch("/api/search", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ query }),
      });
      const json = await resp.json();
      if (json.ids && json.ids.length > 0) {
        setResults(products.filter((p) => json.ids.includes(p.id)));
      } else {
        setResults([]);
        setError("No products found matching your AI search.");
      }
    } catch {
      setError("AI search failed. Please try again.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="container">
      <h1>AI Product Catalog</h1>

      <form onSubmit={aiSearch} className="row" aria-label="AI search form">
        <input
          className="input"
          placeholder='Try: "comfortable running shoes under $100" or "best rated headphones"'
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          disabled={loading}
          aria-label="AI search input"
        />
        <button type="submit" disabled={loading || !query.trim()}>
          {loading ? (
            <span className="loading-text">Searching...</span>
          ) : (
            <span>
              <i className="icon-search"></i> AI Search
            </span>
          )}
        </button>
      </form>

      <div className="filters" aria-label="Filters section">
        <select
          value={category}
          onChange={(e) => setCategory(e.target.value)}
          aria-label="Filter by category"
          disabled={loading}
        >
          {categories.map((c) => (
            <option key={c} value={c}>
              {c}
            </option>
          ))}
        </select>

        <input
          type="number"
          min="0"
          step="0.01"
          placeholder="Max price ($)"
          value={maxPrice}
          onChange={(e) => setMaxPrice(e.target.value)}
          aria-label="Max price filter"
          disabled={loading}
        />

        <button onClick={applyFilter} disabled={loading} type="button">
          Apply Filters
        </button>
      </div>

      {error && <p className="error-message" role="alert">{error}</p>}

      {loading && results.length === 0 ? (
        <div className="loading">Loading products...</div>
      ) : results.length === 0 && !error ? (
        <div className="empty-state">
          <h3>No products found</h3>
          <p>Try adjusting your search or filters</p>
        </div>
      ) : (
        <ul className="products">
          {results.map((p) => (
            <li key={p.id}>
              <b>{p.name}</b>
              <span className="price">${p.price.toFixed(2)}</span>
              <i>{p.category}</i>
              <div>{p.description}</div>
              <div className="rating">Rating: {p.rating} ★</div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}