import React, { useEffect, useState } from 'react';

function App() {
  const [products, setProducts] = useState([]);

  useEffect(() => {
    fetch('http://localhost:8080/api/ai/products')
      .then(res => res.json())
      .then(data => setProducts(data));
  }, []);

  return (
    <div style={{ padding: '20px' }}>
      <h1>AI Product Catalog</h1>
      <ul>
        {products.map(p => (
          <li key={p.id}>{p.name} - {p.price}</li>
        ))}
      </ul>
    </div>
  );
}

export default App;
