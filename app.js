// State Variables
let selectedContinent = 'All';
let selectedVibe = 'All';
let searchTerm = '';
let selectedDestination = null;

let activeModalAttraction = null;
let activeModalGalleryIndex = 0;

let wishlist = JSON.parse(localStorage.getItem('wanderlust_wishlist') || '[]');

let mapInstance = null;
let markersMap = {};

// Initialize App on DOM Content Loaded
document.addEventListener('DOMContentLoaded', () => {
  initMap();
  renderFilterTabs();
  renderQuickChips();
  renderCatalog();
  renderFooterLinks();
  updateWishlistUI();

  // Search Input Listener
  const searchInput = document.getElementById('search-input');
  if (searchInput) {
    searchInput.addEventListener('input', (e) => {
      searchTerm = e.target.value.toLowerCase().trim();
      renderCatalog();
    });
  }

  // Wishlist Drawer Listeners
  document.getElementById('open-wishlist-btn')?.addEventListener('click', openWishlistDrawer);
});

// Helper: Scroll to Section
function scrollToSection(id) {
  const el = document.getElementById(id);
  if (el) el.scrollIntoView({ behavior: 'smooth' });
}

// 1. Initialize Leaflet World Map
function initMap() {
  const mapCanvas = document.getElementById('leaflet-map-canvas');
  if (!mapCanvas) return;

  mapInstance = L.map('leaflet-map-canvas', {
    center: [20.0, 15.0],
    zoom: 3,
    zoomControl: true
  });

  L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
    attribution: '&copy; OpenStreetMap contributors &copy; CARTO',
    subdomains: 'abcd',
    maxZoom: 19
  }).addTo(mapInstance);

  // Map Click Listener
  mapInstance.on('click', (e) => {
    const { lat, lng } = e.latlng;
    
    // Find closest destination
    let closest = null;
    let minDistance = Infinity;

    DESTINATIONS.forEach(dest => {
      const dLat = dest.coordinates[0] - lat;
      const dLng = dest.coordinates[1] - lng;
      const dist = Math.sqrt(dLat * dLat + dLng * dLng);
      if (dist < minDistance) {
        minDistance = dist;
        closest = dest;
      }
    });

    const banner = document.getElementById('custom-coords-banner');
    const coordsTag = document.getElementById('picked-coords-tag');

    if (coordsTag) {
      coordsTag.style.display = 'inline-flex';
      coordsTag.innerText = `📍 Picked Coords: ${lat.toFixed(2)}°N, ${lng.toFixed(2)}°E`;
    }

    if (minDistance < 3.5 && closest) {
      if (banner) banner.style.display = 'none';
      selectDestination(closest);
    } else {
      if (banner) {
        banner.style.display = 'block';
        document.getElementById('custom-coords-text').innerText = `Custom Location Picked (${lat.toFixed(2)}°N, ${lng.toFixed(2)}°E)`;
      }
    }
  });

  // Render Map Markers
  renderMapMarkers();
}

function renderMapMarkers() {
  if (!mapInstance) return;

  // Clear existing
  Object.values(markersMap).forEach(m => mapInstance.removeLayer(m));
  markersMap = {};

  DESTINATIONS.forEach(dest => {
    const isSelected = selectedDestination?.id === dest.id;
    const iconHtml = `<div class="marker-pin" style="${isSelected ? 'background: linear-gradient(135deg, #F59E0B, #EC4899); scale: 1.25;' : ''}"></div>`;
    
    const customIcon = L.divIcon({
      className: 'custom-map-marker',
      html: iconHtml,
      iconSize: [38, 38],
      iconAnchor: [19, 38]
    });

    const popupContent = `
      <div style="padding:6px; text-align:center; cursor:pointer;" onclick="selectDestinationById('${dest.id}')">
        <div style="font-weight:700; font-family:var(--font-heading); font-size:1rem; color:#FFF; margin-bottom:4px;">
          ${dest.name}, ${dest.country}
        </div>
        <img src="${dest.heroImage}" style="width:100%; height:85px; object-fit:cover; border-radius:8px; margin-bottom:6px;" />
        <div style="font-size:0.75rem; color:#10B981; font-weight:600;">
          ⭐ ${dest.rating} • ${dest.attractions.length} Tourist Spots
        </div>
        <div style="margin-top:6px; font-size:0.72rem; color:#9CA3AF; text-transform:uppercase;">
          👉 Click to view tourist locations
        </div>
      </div>
    `;

    const marker = L.marker(dest.coordinates, { icon: customIcon })
      .addTo(mapInstance)
      .bindPopup(popupContent);

    marker.on('click', () => {
      selectDestination(dest);
    });

    markersMap[dest.id] = marker;
  });
}

// 2. Select Destination Handler
function selectDestinationById(id) {
  const dest = DESTINATIONS.find(d => d.id === id);
  if (dest) selectDestination(dest);
}

function selectDestination(dest) {
  selectedDestination = dest;

  // Fly map to selected coordinates
  if (mapInstance) {
    mapInstance.flyTo(dest.coordinates, 8, { duration: 1.5 });
    if (markersMap[dest.id]) {
      markersMap[dest.id].openPopup();
    }
  }

  // Update detail view HTML
  renderDestinationDetail(dest);

  // Hide catalog, show detail section
  document.getElementById('catalog-section').style.display = 'none';
  const detailSection = document.getElementById('destination-detail-section');
  detailSection.style.display = 'block';

  // Scroll to detail view
  detailSection.scrollIntoView({ behavior: 'smooth' });

  // Re-init lucide icons for newly added HTML
  if (window.lucide) lucide.createIcons();
}

function backToCatalog() {
  selectedDestination = null;
  document.getElementById('destination-detail-section').style.display = 'none';
  document.getElementById('catalog-section').style.display = 'block';
  scrollToSection('catalog-section');
  renderMapMarkers();
}

// 3. Render Filter Tabs & Chips
function renderFilterTabs() {
  const contContainer = document.getElementById('continent-tabs');
  const vibeContainer = document.getElementById('vibe-tabs');

  if (contContainer) {
    contContainer.innerHTML = CONTINENTS.map(c => `
      <button class="tab-btn ${selectedContinent === c ? 'active' : ''}" onclick="setContinent('${c}')">
        ${c}
      </button>
    `).join('');
  }

  if (vibeContainer) {
    vibeContainer.innerHTML = VIBES.map(v => `
      <button class="tab-btn ${selectedVibe === v ? 'active' : ''}" onclick="setVibe('${v}')">
        ${v}
      </button>
    `).join('');
  }
}

function setContinent(c) {
  selectedContinent = c;
  renderFilterTabs();
  renderCatalog();
}

function setVibe(v) {
  selectedVibe = v;
  renderFilterTabs();
  renderCatalog();
}

function resetFilters() {
  selectedContinent = 'All';
  selectedVibe = 'All';
  searchTerm = '';
  const input = document.getElementById('search-input');
  if (input) input.value = '';
  renderFilterTabs();
  renderCatalog();
}

function renderQuickChips() {
  const wrapper = document.getElementById('quick-chips');
  if (!wrapper) return;

  wrapper.innerHTML = DESTINATIONS.slice(0, 7).map(d => `
    <button class="chip-btn" onclick="selectDestinationById('${d.id}')">
      <i data-lucide="map-pin" style="width:13px; height:13px; color:#34D399;"></i>
      <span>${d.name}, ${d.country}</span>
    </button>
  `).join('');
}

// 4. Render Catalog Grid
function renderCatalog() {
  const grid = document.getElementById('destinations-grid');
  const countBadge = document.getElementById('location-count-badge');
  const resetBtn = document.getElementById('reset-filters-btn');

  if (!grid) return;

  const filtered = DESTINATIONS.filter(dest => {
    const matchesContinent = selectedContinent === 'All' || dest.continent === selectedContinent;
    const matchesVibe = selectedVibe === 'All' || dest.vibe === selectedVibe;
    const matchesSearch = !searchTerm || 
      dest.name.toLowerCase().includes(searchTerm) ||
      dest.country.toLowerCase().includes(searchTerm) ||
      dest.tagline.toLowerCase().includes(searchTerm) ||
      dest.attractions.some(a => a.title.toLowerCase().includes(searchTerm));

    return matchesContinent && matchesVibe && matchesSearch;
  });

  if (countBadge) countBadge.innerText = filtered.length;
  if (resetBtn) {
    resetBtn.style.display = (selectedContinent !== 'All' || selectedVibe !== 'All' || searchTerm !== '') ? 'flex' : 'none';
  }

  if (filtered.length === 0) {
    grid.innerHTML = `
      <div style="grid-column: 1/-1; text-align:center; padding:3rem; background:rgba(255,255,255,0.03); border-radius:24px; border:1px solid var(--border-glass)">
        <i data-lucide="globe" style="width:48px; height:48px; color:#9CA3AF; margin-bottom:1rem;"></i>
        <h3>No tourist locations match your active filter.</h3>
        <p style="color:var(--text-muted); margin-top:0.5rem;">Try resetting your search query or continent/vibe filters.</p>
      </div>
    `;
    if (window.lucide) lucide.createIcons();
    return;
  }

  grid.innerHTML = filtered.map(dest => `
    <div class="glass-card" onclick="selectDestinationById('${dest.id}')" style="border-radius:20px; overflow:hidden; cursor:pointer; display:flex; flex-direction:column;">
      <div style="height:210px; width:100%; position:relative; overflow:hidden;">
        <img src="${dest.heroImage}" alt="${dest.name}" style="width:100%; height:100%; object-fit:cover; transition:transform 0.5s ease;" onmouseenter="this.style.transform='scale(1.08)'" onmouseleave="this.style.transform='scale(1.0)'" />
        <div style="position:absolute; top:12px; left:12px; right:12px; display:flex; justify-content:space-between; align-items:center;">
          <span class="badge badge-primary"><i data-lucide="globe" style="width:11px;"></i> ${dest.continent}</span>
          <span style="background:rgba(9,13,22,0.8); backdrop-filter:blur(8px); padding:0.3rem 0.65rem; border-radius:16px; font-size:0.78rem; font-weight:700; color:#FBBF24; display:flex; align-items:center; gap:0.25rem;">
            ★ ${dest.rating}
          </span>
        </div>
        <div style="position:absolute; bottom:12px; left:12px;">
          <span class="badge badge-amber">${dest.vibe}</span>
        </div>
      </div>

      <div style="padding:1.4rem; flex:1; display:flex; flex-direction:column; justify-content:space-between;">
        <div>
          <div style="display:flex; align-items:center; gap:0.4rem; margin-bottom:0.25rem; font-size:0.85rem; color:var(--text-muted); font-weight:600;">
            <i data-lucide="map-pin" style="width:15px; color:#10B981;"></i> ${dest.country}
          </div>
          <h3 style="font-size:1.4rem; font-weight:700; margin-bottom:0.4rem; color:#FFF;">${dest.name}</h3>
          <p style="font-size:0.86rem; color:var(--text-muted); line-height:1.5; margin-bottom:1rem; display:-webkit-box; -webkit-line-clamp:2; -webkit-box-orient:vertical; overflow:hidden;">
            ${dest.tagline}
          </p>
        </div>

        <div style="padding-top:0.85rem; border-top:1px solid rgba(255,255,255,0.06); display:flex; align-items:center; justify-content:space-between;">
          <div style="display:flex; align-items:center; gap:0.4rem; font-size:0.82rem; color:var(--text-muted);">
            <i data-lucide="camera" style="width:14px; color:#38BDF8;"></i> <strong>${dest.attractions.length}</strong> Tourist Spots
          </div>
          <div style="display:flex; align-items:center; gap:0.3rem; font-size:0.85rem; font-weight:600; color:#34D399;">
            <span>Explore</span> <i data-lucide="arrow-right" style="width:15px;"></i>
          </div>
        </div>
      </div>
    </div>
  `).join('');

  if (window.lucide) lucide.createIcons();
}

// 5. Render Destination Detail View
function renderDestinationDetail(dest) {
  const container = document.getElementById('destination-detail-section');
  if (!container) return;

  container.innerHTML = `
    <button className="btn-secondary" onclick="backToCatalog()" style="margin-bottom:1.5rem;">
      <i data-lucide="arrow-left"></i> Back to Global Map & Catalog
    </button>

    <div class="glass-panel" style="position:relative; border-radius:28px; overflow:hidden; margin-bottom:2.5rem; border:1px solid rgba(16,185,129,0.3);">
      <div style="height:320px; width:100%; position:relative;">
        <img src="${dest.heroImage}" alt="${dest.name}" style="width:100%; height:100%; object-fit:cover;" />
        <div style="position:absolute; inset:0; background:linear-gradient(to top, rgba(9,13,22,0.98) 0%, rgba(9,13,22,0.4) 60%, rgba(0,0,0,0.1) 100%);"></div>
        <div style="position:absolute; bottom:2rem; left:2.5rem; right:2.5rem; display:flex; justify-content:space-between; align-items:flex-end; flex-wrap:wrap; gap:1.5rem;">
          <div>
            <div class="badge badge-primary" style="margin-bottom:0.5rem;"><i data-lucide="map-pin"></i> ${dest.country} • ${dest.continent}</div>
            <h1 style="font-size:3.2rem; font-weight:800; color:#FFF; line-height:1.1;">${dest.name}</h1>
            <p style="font-size:1.1rem; color:#D1D5DB; max-width:650px; margin-top:0.4rem;">${dest.tagline}</p>
          </div>

          <div style="background:rgba(18,26,43,0.85); backdrop-filter:blur(12px); padding:1rem 1.4rem; border-radius:20px; border:1px solid rgba(255,255,255,0.1); display:flex; gap:1.5rem; align-items:center;">
            <div>
              <div style="font-size:0.72rem; color:var(--text-muted); text-transform:uppercase;">Best Time</div>
              <div style="font-size:0.9rem; color:#FBBF24; font-weight:700;">📅 ${dest.bestMonths}</div>
            </div>
            <div style="width:1px; height:30px; background:rgba(255,255,255,0.1);"></div>
            <div>
              <div style="font-size:0.72rem; color:var(--text-muted); text-transform:uppercase;">Avg Temp</div>
              <div style="font-size:0.9rem; color:#38BDF8; font-weight:700;">☀️ ${dest.weatherAvg}</div>
            </div>
            <div style="width:1px; height:30px; background:rgba(255,255,255,0.1);"></div>
            <div>
              <div style="font-size:0.72rem; color:var(--text-muted); text-transform:uppercase;">Currency</div>
              <div style="font-size:0.9rem; color:#34D399; font-weight:700;">💵 ${dest.currency}</div>
            </div>
          </div>
        </div>
      </div>

      <div style="padding:2rem 2.5rem;">
        <p style="font-size:1.05rem; color:var(--text-main); line-height:1.7; margin-bottom:1.5rem;">${dest.description}</p>
        <div style="background:rgba(16,185,129,0.05); border:1px solid rgba(16,185,129,0.2); border-radius:16px; padding:1.2rem 1.5rem;">
          <div style="font-size:0.88rem; font-weight:700; color:#34D399; margin-bottom:0.6rem; display:flex; align-items:center; gap:0.4rem;">
            <i data-lucide="sparkles" style="width:16px;"></i> Essential Travel Tips for ${dest.name}:
          </div>
          <ul style="display:grid; grid-template-columns:repeat(auto-fit, minmax(260px, 1fr)); gap:0.75rem; list-style:none;">
            ${dest.quickTips.map(t => `<li style="display:flex; gap:0.5rem; font-size:0.86rem; color:var(--text-muted);"><i data-lucide="check-circle-2" style="width:16px; color:#10B981;"></i> ${t}</li>`).join('')}
          </ul>
        </div>
      </div>
    </div>

    <!-- Attractions Section -->
    <div style="margin-bottom:3.5rem;">
      <div style="margin-bottom:1.5rem;">
        <h2 class="section-title"><i data-lucide="camera" style="color:#10B981;"></i> Top Tourist Locations in ${dest.name}</h2>
        <p class="section-subtitle">Must-visit landmarks, historic sites, and photo spots</p>
      </div>

      <div class="attraction-grid">
        ${dest.attractions.map(att => renderAttractionCardHtml(att, dest.name)).join('')}
      </div>
    </div>

    <!-- Sample Itinerary Section -->
    <div class="glass-panel" style="padding:2rem 2.5rem; border-radius:24px;">
      <h2 style="font-size:1.6rem; font-weight:700; color:#FFF; margin-bottom:0.3rem;">Suggested Travel Itinerary for ${dest.name}</h2>
      <p style="color:var(--text-muted); font-size:0.9rem; margin-bottom:1.5rem;">Day-by-day plan so you don't miss key spots</p>
      <div style="display:flex; flex-direction:column; gap:1rem;">
        ${dest.itinerary1Day.map((step, i) => `
          <div style="background:rgba(255,255,255,0.03); border:1px solid var(--border-glass); padding:1rem 1.4rem; border-radius:16px; display:flex; align-items:center; gap:1rem;">
            <div style="width:36px; height:36px; border-radius:50%; background:rgba(16,185,129,0.2); color:#34D399; display:flex; align-items:center; justify-content:center; font-weight:bold;">${i+1}</div>
            <div style="font-size:0.95rem; color:#F3F4F6; font-weight:500;">${step}</div>
          </div>
        `).join('')}
      </div>
    </div>
  `;

  if (window.lucide) lucide.createIcons();
}

function renderAttractionCardHtml(att, destName) {
  const isWishlisted = wishlist.some(w => w.id === att.id);
  const mapSearchUrl = `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(att.title + " " + destName)}`;

  return `
    <div class="glass-card" style="border-radius:20px; overflow:hidden; display:flex; flex-direction:column;">
      <div style="height:220px; width:100%; position:relative;">
        <img src="${att.image}" alt="${att.title}" style="width:100%; height:100%; object-fit:cover;" />
        <div style="position:absolute; top:12px; left:12px;">
          <span class="badge badge-primary">${att.category}</span>
        </div>
        <button onclick="event.stopPropagation(); toggleWishlist('${att.id}')" style="position:absolute; top:12px; right:12px; width:38px; height:38px; border-radius:50%; background:rgba(9,13,22,0.75); backdrop-filter:blur(8px); border:1px solid rgba(255,255,255,0.15); display:flex; align-items:center; justify-content:center; cursor:pointer;">
          <i data-lucide="heart" style="width:18px; color:${isWishlisted ? '#EC4899' : '#FFF'}; fill:${isWishlisted ? '#EC4899' : 'none'};"></i>
        </button>
        <button onclick="openAttractionModal('${att.id}')" style="position:absolute; bottom:12px; right:12px; background:rgba(0,0,0,0.75); border:1px solid rgba(255,255,255,0.2); color:#FFF; padding:0.35rem 0.75rem; border-radius:16px; font-size:0.78rem; font-weight:600; cursor:pointer; display:flex; align-items:center; gap:0.35rem;">
          <i data-lucide="camera" style="width:13px; color:#34D399;"></i> Photos
        </button>
      </div>

      <div style="padding:1.4rem; flex:1; display:flex; flex-direction:column; justify-content:space-between;">
        <div>
          <div style="display:flex; align-items:center; justify-content:space-between; margin-bottom:0.4rem;">
            <h3 style="font-size:1.3rem; font-weight:700; color:#FFF;">${att.title}</h3>
            <div style="font-size:0.85rem; color:#FBBF24; font-weight:700;">★ ${att.rating}</div>
          </div>
          <div style="display:flex; gap:1rem; font-size:0.82rem; color:var(--text-muted); margin-bottom:0.85rem;">
            <span>⏱️ ${att.duration}</span>
            <span>🏷️ ${att.price}</span>
          </div>
          <p style="font-size:0.88rem; color:var(--text-muted); line-height:1.5; margin-bottom:1rem;">${att.description}</p>
          ${att.insiderTip ? `
            <div style="background:rgba(245,158,11,0.08); border-left:3px solid #F59E0B; padding:0.75rem 0.9rem; border-radius:0 10px 10px 0; font-size:0.82rem; color:#FDE68A; margin-bottom:1rem;">
              <strong>Tip:</strong> ${att.insiderTip}
            </div>
          ` : ''}
        </div>

        <div style="display:flex; gap:0.75rem; padding-top:0.85rem; border-top:1px solid rgba(255,255,255,0.06);">
          <button class="btn-primary" onclick="openAttractionModal('${att.id}')" style="flex:1; justify-content:center;">Spot Details</button>
          <a href="${mapSearchUrl}" target="_blank" class="btn-secondary" style="padding:0.65rem;"><i data-lucide="external-link" style="width:16px; color:#38BDF8;"></i></a>
        </div>
      </div>
    </div>
  `;
}

// 6. Lightbox Modal
function openAttractionModal(attId) {
  let found = null;
  DESTINATIONS.forEach(d => {
    const a = d.attractions.find(x => x.id === attId);
    if (a) found = a;
  });

  if (!found) return;
  activeModalAttraction = found;
  activeModalGalleryIndex = 0;

  updateModalImage();
  
  const content = document.getElementById('modal-body-content');
  if (content) {
    const isWishlisted = wishlist.some(w => w.id === found.id);
    content.innerHTML = `
      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:1rem;">
        <div>
          <span class="badge badge-primary">${found.category}</span>
          <h2 style="font-size:2rem; font-weight:800; color:#FFF; margin-top:0.4rem;">${found.title}</h2>
        </div>
        <button className="btn-secondary" onclick="toggleWishlist('${found.id}')">
          <i data-lucide="heart" style="color:${isWishlisted ? '#EC4899' : '#FFF'}; fill:${isWishlisted ? '#EC4899' : 'none'};"></i>
          ${isWishlisted ? 'Saved' : 'Save Spot'}
        </button>
      </div>
      <p style="font-size:1rem; color:var(--text-main); line-height:1.7;">${found.description}</p>
    `;
  }

  document.getElementById('attraction-modal').style.display = 'flex';
  if (window.lucide) lucide.createIcons();
}

function updateModalImage() {
  if (!activeModalAttraction) return;
  const imgs = activeModalAttraction.gallery || [activeModalAttraction.image];
  const imgEl = document.getElementById('modal-gallery-img');
  if (imgEl) imgEl.src = imgs[activeModalGalleryIndex];

  const dots = document.getElementById('modal-thumb-dots');
  if (dots) {
    dots.innerHTML = imgs.map((_, i) => `
      <div class="dot-thumb ${i === activeModalGalleryIndex ? 'active' : ''}" onclick="setModalImageIndex(${i})"></div>
    `).join('');
  }
}

function prevModalImage() {
  if (!activeModalAttraction) return;
  const imgs = activeModalAttraction.gallery || [activeModalAttraction.image];
  activeModalGalleryIndex = (activeModalGalleryIndex - 1 + imgs.length) % imgs.length;
  updateModalImage();
}

function nextModalImage() {
  if (!activeModalAttraction) return;
  const imgs = activeModalAttraction.gallery || [activeModalAttraction.image];
  activeModalGalleryIndex = (activeModalGalleryIndex + 1) % imgs.length;
  updateModalImage();
}

function setModalImageIndex(i) {
  activeModalGalleryIndex = i;
  updateModalImage();
}

function closeAttractionModal() {
  document.getElementById('attraction-modal').style.display = 'none';
}

// 7. LocalStorage Wishlist
function toggleWishlist(attId) {
  let found = null;
  DESTINATIONS.forEach(d => {
    const a = d.attractions.find(x => x.id === attId);
    if (a) found = a;
  });
  if (!found) return;

  const exists = wishlist.some(w => w.id === attId);
  if (exists) {
    wishlist = wishlist.filter(w => w.id !== attId);
  } else {
    wishlist.push(found);
  }

  localStorage.setItem('wanderlust_wishlist', JSON.stringify(wishlist));
  updateWishlistUI();

  if (selectedDestination) renderDestinationDetail(selectedDestination);
  if (activeModalAttraction && activeModalAttraction.id === attId) openAttractionModal(attId);
}

function updateWishlistUI() {
  const badge = document.getElementById('wishlist-badge');
  const countSpan = document.getElementById('drawer-wishlist-count');
  
  if (badge) {
    badge.innerText = wishlist.length;
    badge.style.display = wishlist.length > 0 ? 'flex' : 'none';
  }
  if (countSpan) countSpan.innerText = wishlist.length;

  const drawerBody = document.getElementById('wishlist-drawer-body');
  if (drawerBody) {
    if (wishlist.length === 0) {
      drawerBody.innerHTML = `
        <div style="text-align:center; padding:3rem 1rem; color:var(--text-muted)">
          <i data-lucide="heart" style="width:44px; height:44px; color:rgba(255,255,255,0.15); margin-bottom:1rem;"></i>
          <p style="font-weight:600;">No saved tourist locations yet.</p>
        </div>
      `;
    } else {
      drawerBody.innerHTML = wishlist.map(w => `
        <div style="background:rgba(255,255,255,0.03); border:1px solid var(--border-glass); border-radius:16px; padding:0.85rem; display:flex; gap:0.85rem; align-items:center;">
          <img src="${w.image}" style="width:60px; height:60px; border-radius:12px; object-fit:cover;" />
          <div style="flex:1;">
            <div style="font-size:0.72rem; color:#10B981; font-weight:600;">${w.category}</div>
            <div style="font-size:0.92rem; font-weight:700; color:#FFF; cursor:pointer;" onclick="openAttractionModal('${w.id}')">${w.title}</div>
            <div style="font-size:0.78rem; color:var(--text-muted);">★ ${w.rating} • ${w.price}</div>
          </div>
          <button onclick="toggleWishlist('${w.id}')" style="background:none; border:none; color:#EF4444; cursor:pointer;"><i data-lucide="trash-2" style="width:16px;"></i></button>
        </div>
      `).join('');
    }
  }

  if (window.lucide) lucide.createIcons();
}

function openWishlistDrawer() {
  updateWishlistUI();
  document.getElementById('wishlist-drawer').style.display = 'flex';
}

function closeWishlistDrawer() {
  document.getElementById('wishlist-drawer').style.display = 'none';
}

function clearAllWishlist() {
  wishlist = [];
  localStorage.setItem('wanderlust_wishlist', JSON.stringify(wishlist));
  updateWishlistUI();
}

function renderFooterLinks() {
  const container = document.getElementById('footer-links');
  if (!container) return;

  container.innerHTML = DESTINATIONS.slice(0, 5).map(d => `
    <span onclick="selectDestinationById('${d.id}')" style="cursor:pointer;" onmouseenter="this.style.color='#10B981'" onmouseleave="this.style.color='var(--text-muted)'">
      • ${d.name} Tourist Locations (${d.country})
    </span>
  `).join('');
}
