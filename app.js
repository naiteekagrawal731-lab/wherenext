// State Variables
let selectedContinent = 'All';
let selectedVibe = 'All';
let searchTerm = '';
let selectedDestination = null;

let activeModalAttraction = null;
let activeModalGalleryIndex = 0;
let activeModalTab = 'details'; // 'details' | 'gmaps' | 'ai'

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
  loadSavedApiKey();

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

// 1. Initialize Leaflet World Map (Light Voyager Theme)
function initMap() {
  const mapCanvas = document.getElementById('leaflet-map-canvas');
  if (!mapCanvas) return;

  mapInstance = L.map('leaflet-map-canvas', {
    center: [20.0, 15.0],
    zoom: 3,
    zoomControl: true
  });

  // CartoDB Voyager Light Tiles
  L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>',
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
    const gmapsBtn = document.getElementById('custom-coords-gmaps-btn');

    if (coordsTag) {
      coordsTag.style.display = 'inline-flex';
      coordsTag.innerText = `📍 Picked: ${lat.toFixed(2)}°N, ${lng.toFixed(2)}°E`;
    }

    if (gmapsBtn) {
      gmapsBtn.onclick = () => {
        window.open(`https://www.google.com/maps/search/?api=1&query=${lat},${lng}`, '_blank');
      };
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

  // Clear existing markers
  Object.values(markersMap).forEach(m => mapInstance.removeLayer(m));
  markersMap = {};

  DESTINATIONS.forEach(dest => {
    const isSelected = selectedDestination?.id === dest.id;
    const iconHtml = `<div class="marker-pin" style="${isSelected ? 'background: linear-gradient(135deg, #F59E0B, #DB2777); scale: 1.25;' : ''}"></div>`;
    
    const customIcon = L.divIcon({
      className: 'custom-map-marker',
      html: iconHtml,
      iconSize: [38, 38],
      iconAnchor: [19, 38]
    });

    const popupContent = `
      <div style="padding:4px; text-align:center; cursor:pointer;" onclick="selectDestinationById('${dest.id}')">
        <div style="font-weight:700; font-family:var(--font-heading); font-size:0.95rem; color:#0F172A; margin-bottom:4px;">
          ${dest.name}, ${dest.country}
        </div>
        <img src="${dest.heroImage}" style="width:100%; height:85px; object-fit:cover; border-radius:8px; margin-bottom:6px;" />
        <div style="font-size:0.75rem; color:#059669; font-weight:600;">
          ⭐ ${dest.rating} • ${dest.attractions.length} Tourist Spots
        </div>
        <div style="margin-top:4px; font-size:0.72rem; color:#0284C7; font-weight:600; text-transform:uppercase;">
          👉 Click to explore tourist locations
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
      <i data-lucide="map-pin" style="width:13px; height:13px; color:#10B981;"></i>
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
      <div style="grid-column: 1/-1; text-align:center; padding:3rem; background:#FFFFFF; border-radius:24px; border:1px solid #E2E8F0">
        <i data-lucide="globe" style="width:48px; height:48px; color:#94A3B8; margin-bottom:1rem;"></i>
        <h3 style="color:#0F172A;">No tourist locations match your active filter.</h3>
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
          <span style="background:rgba(255,255,255,0.9); backdrop-filter:blur(8px); padding:0.3rem 0.65rem; border-radius:16px; font-size:0.78rem; font-weight:700; color:#D97706; display:flex; align-items:center; gap:0.25rem; box-shadow:0 2px 8px rgba(0,0,0,0.1);">
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
          <h3 style="font-size:1.4rem; font-weight:700; margin-bottom:0.4rem; color:#0F172A;">${dest.name}</h3>
          <p style="font-size:0.86rem; color:var(--text-muted); line-height:1.5; margin-bottom:1rem; display:-webkit-box; -webkit-line-clamp:2; -webkit-box-orient:vertical; overflow:hidden;">
            ${dest.tagline}
          </p>
        </div>

        <div style="padding-top:0.85rem; border-top:1px solid #E2E8F0; display:flex; align-items:center; justify-content:space-between;">
          <div style="display:flex; align-items:center; gap:0.4rem; font-size:0.82rem; color:var(--text-muted);">
            <i data-lucide="camera" style="width:14px; color:#0284C7;"></i> <strong>${dest.attractions.length}</strong> Tourist Spots
          </div>
          <div style="display:flex; align-items:center; gap:0.3rem; font-size:0.85rem; font-weight:600; color:#059669;">
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

  const gmapsSearchUrl = `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(dest.name + " " + dest.country)}`;

  container.innerHTML = `
    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:1.5rem; flex-wrap:wrap; gap:1rem;">
      <button class="btn-secondary" onclick="backToCatalog()">
        <i data-lucide="arrow-left"></i> Back to Global Map & Catalog
      </button>

      <div style="display:flex; gap:0.75rem;">
        <a href="${gmapsSearchUrl}" target="_blank" class="btn-secondary">
          <i data-lucide="map" style="color:#0284C7;"></i> View on Google Maps
        </a>
        <button class="btn-ai" onclick="openAiGuideModalForDest('${dest.name}')">
          <i data-lucide="sparkles"></i> Ask Google AI Guide
        </button>
      </div>
    </div>

    <div class="glass-panel" style="position:relative; border-radius:28px; overflow:hidden; margin-bottom:2.5rem; border:1px solid #E2E8F0; background:#FFFFFF;">
      <div style="height:320px; width:100%; position:relative;">
        <img src="${dest.heroImage}" alt="${dest.name}" style="width:100%; height:100%; object-fit:cover;" />
        <div style="position:absolute; inset:0; background:linear-gradient(to top, rgba(15,23,42,0.9) 0%, rgba(15,23,42,0.3) 60%, rgba(0,0,0,0) 100%);"></div>
        <div style="position:absolute; bottom:2rem; left:2.5rem; right:2.5rem; display:flex; justify-content:space-between; align-items:flex-end; flex-wrap:wrap; gap:1.5rem;">
          <div>
            <div class="badge badge-primary" style="margin-bottom:0.5rem; background:rgba(255,255,255,0.9); color:#047857;"><i data-lucide="map-pin"></i> ${dest.country} • ${dest.continent}</div>
            <h1 style="font-size:3.2rem; font-weight:800; color:#FFFFFF; line-height:1.1;">${dest.name}</h1>
            <p style="font-size:1.1rem; color:#F1F5F9; max-width:650px; margin-top:0.4rem;">${dest.tagline}</p>
          </div>

          <div style="background:rgba(255,255,255,0.92); backdrop-filter:blur(12px); padding:1rem 1.4rem; border-radius:20px; border:1px solid #E2E8F0; display:flex; gap:1.5rem; align-items:center; box-shadow:0 10px 25px rgba(0,0,0,0.1);">
            <div>
              <div style="font-size:0.72rem; color:var(--text-muted); text-transform:uppercase; font-weight:600;">Best Time</div>
              <div style="font-size:0.9rem; color:#B45309; font-weight:700;">📅 ${dest.bestMonths}</div>
            </div>
            <div style="width:1px; height:30px; background:#CBD5E1;"></div>
            <div>
              <div style="font-size:0.72rem; color:var(--text-muted); text-transform:uppercase; font-weight:600;">Avg Temp</div>
              <div style="font-size:0.9rem; color:#0369A1; font-weight:700;">☀️ ${dest.weatherAvg}</div>
            </div>
            <div style="width:1px; height:30px; background:#CBD5E1;"></div>
            <div>
              <div style="font-size:0.72rem; color:var(--text-muted); text-transform:uppercase; font-weight:600;">Currency</div>
              <div style="font-size:0.9rem; color:#047857; font-weight:700;">💵 ${dest.currency}</div>
            </div>
          </div>
        </div>
      </div>

      <div style="padding:2rem 2.5rem; background:#FFFFFF;">
        <p style="font-size:1.05rem; color:var(--text-main); line-height:1.7; margin-bottom:1.5rem;">${dest.description}</p>
        <div style="background:#F0FDF4; border:1px solid rgba(16,185,129,0.3); border-radius:16px; padding:1.2rem 1.5rem;">
          <div style="font-size:0.88rem; font-weight:700; color:#047857; margin-bottom:0.6rem; display:flex; align-items:center; gap:0.4rem;">
            <i data-lucide="sparkles" style="width:16px;"></i> Essential Travel Tips for ${dest.name}:
          </div>
          <ul style="display:grid; grid-template-columns:repeat(auto-fit, minmax(260px, 1fr)); gap:0.75rem; list-style:none;">
            ${dest.quickTips.map(t => `<li style="display:flex; gap:0.5rem; font-size:0.86rem; color:#1E293B;"><i data-lucide="check-circle-2" style="width:16px; color:#10B981;"></i> ${t}</li>`).join('')}
          </ul>
        </div>
      </div>
    </div>

    <!-- Attractions Section -->
    <div style="margin-bottom:3.5rem;">
      <div style="margin-bottom:1.5rem;">
        <h2 class="section-title"><i data-lucide="camera" style="color:#10B981;"></i> Top Tourist Locations in ${dest.name}</h2>
        <p class="section-subtitle">Must-visit landmarks, historic sites, Google Maps directions, and photo spots</p>
      </div>

      <div class="attraction-grid">
        ${dest.attractions.map(att => renderAttractionCardHtml(att, dest.name)).join('')}
      </div>
    </div>

    <!-- Sample Itinerary Section -->
    <div class="glass-panel" style="padding:2rem 2.5rem; border-radius:24px; background:#FFFFFF;">
      <h2 style="font-size:1.6rem; font-weight:700; color:#0F172A; margin-bottom:0.3rem;">Suggested Travel Itinerary for ${dest.name}</h2>
      <p style="color:var(--text-muted); font-size:0.9rem; margin-bottom:1.5rem;">Day-by-day plan so you don't miss key spots</p>
      <div style="display:flex; flex-direction:column; gap:1rem;">
        ${dest.itinerary1Day.map((step, i) => `
          <div style="background:#F8FAFC; border:1px solid #E2E8F0; padding:1rem 1.4rem; border-radius:16px; display:flex; align-items:center; gap:1rem;">
            <div style="width:36px; height:36px; border-radius:50%; background:rgba(16,185,129,0.15); color:#047857; display:flex; align-items:center; justify-content:center; font-weight:bold;">${i+1}</div>
            <div style="font-size:0.95rem; color:#0F172A; font-weight:500;">${step}</div>
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
  const mapDirectionsUrl = `https://www.google.com/maps/dir/?api=1&destination=${encodeURIComponent(att.title + " " + destName)}`;

  return `
    <div class="glass-card" style="border-radius:20px; overflow:hidden; display:flex; flex-direction:column;">
      <div style="height:220px; width:100%; position:relative;">
        <img src="${att.image}" alt="${att.title}" style="width:100%; height:100%; object-fit:cover;" />
        <div style="position:absolute; top:12px; left:12px;">
          <span class="badge badge-primary" style="background:rgba(255,255,255,0.9); color:#047857;">${att.category}</span>
        </div>
        <button onclick="event.stopPropagation(); toggleWishlist('${att.id}')" style="position:absolute; top:12px; right:12px; width:38px; height:38px; border-radius:50%; background:rgba(255,255,255,0.9); backdrop-filter:blur(8px); border:1px solid #CBD5E1; display:flex; align-items:center; justify-content:center; cursor:pointer; box-shadow:0 2px 8px rgba(0,0,0,0.1);">
          <i data-lucide="heart" style="width:18px; color:${isWishlisted ? '#DB2777' : '#64748B'}; fill:${isWishlisted ? '#DB2777' : 'none'};"></i>
        </button>
        <button onclick="openAttractionModal('${att.id}')" style="position:absolute; bottom:12px; right:12px; background:rgba(255,255,255,0.9); border:1px solid #CBD5E1; color:#0F172A; padding:0.35rem 0.75rem; border-radius:16px; font-size:0.78rem; font-weight:600; cursor:pointer; display:flex; align-items:center; gap:0.35rem; box-shadow:0 2px 8px rgba(0,0,0,0.1);">
          <i data-lucide="camera" style="width:13px; color:#059669;"></i> View Details
        </button>
      </div>

      <div style="padding:1.4rem; flex:1; display:flex; flex-direction:column; justify-content:space-between;">
        <div>
          <div style="display:flex; align-items:center; justify-content:space-between; margin-bottom:0.4rem;">
            <h3 style="font-size:1.3rem; font-weight:700; color:#0F172A;">${att.title}</h3>
            <div style="font-size:0.85rem; color:#B45309; font-weight:700;">★ ${att.rating}</div>
          </div>
          <div style="display:flex; gap:1rem; font-size:0.82rem; color:var(--text-muted); margin-bottom:0.85rem;">
            <span>⏱️ ${att.duration}</span>
            <span>🏷️ ${att.price}</span>
          </div>
          <p style="font-size:0.88rem; color:var(--text-muted); line-height:1.5; margin-bottom:1rem;">${att.description}</p>
          ${att.insiderTip ? `
            <div style="background:#FEF3C7; border-left:3px solid #F59E0B; padding:0.75rem 0.9rem; border-radius:0 10px 10px 0; font-size:0.82rem; color:#92400E; margin-bottom:1rem;">
              <strong>Tip:</strong> ${att.insiderTip}
            </div>
          ` : ''}
        </div>

        <div style="display:flex; gap:0.5rem; padding-top:0.85rem; border-top:1px solid #E2E8F0;">
          <button class="btn-primary" onclick="openAttractionModal('${att.id}')" style="flex:1; justify-content:center;">Spot Details</button>
          <a href="${mapSearchUrl}" target="_blank" class="btn-secondary" title="View on Google Maps" style="padding:0.65rem;"><i data-lucide="map" style="width:16px; color:#0284C7;"></i></a>
          <a href="${mapDirectionsUrl}" target="_blank" class="btn-secondary" title="Get Google Directions" style="padding:0.65rem;"><i data-lucide="navigation" style="width:16px; color:#10B981;"></i></a>
        </div>
      </div>
    </div>
  `;
}

// 6. Lightbox & Google Maps Open View Modal
function openAttractionModal(attId, initialTab = 'details') {
  let foundAtt = null;
  let foundDestName = '';

  DESTINATIONS.forEach(d => {
    const a = d.attractions.find(x => x.id === attId);
    if (a) {
      foundAtt = a;
      foundDestName = d.name;
    }
  });

  if (!foundAtt) return;
  activeModalAttraction = foundAtt;
  activeModalGalleryIndex = 0;
  activeModalTab = initialTab;

  renderModalContent(foundAtt, foundDestName);

  document.getElementById('attraction-modal').style.display = 'flex';
  if (window.lucide) lucide.createIcons();
}

function switchModalTab(tabName) {
  activeModalTab = tabName;
  let destName = '';
  DESTINATIONS.forEach(d => {
    if (d.attractions.some(a => a.id === activeModalAttraction.id)) destName = d.name;
  });
  renderModalContent(activeModalAttraction, destName);
  if (window.lucide) lucide.createIcons();
}

function renderModalContent(att, destName) {
  const container = document.getElementById('modal-container-inner');
  if (!container) return;

  const isWishlisted = wishlist.some(w => w.id === att.id);
  const gmapsSearchUrl = `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(att.title + " " + destName)}`;
  const gmapsEmbedUrl = `https://maps.google.com/maps?q=${encodeURIComponent(att.title + " " + destName)}&t=&z=14&ie=UTF8&iwloc=&output=embed`;
  const gmapsDirectionsUrl = `https://www.google.com/maps/dir/?api=1&destination=${encodeURIComponent(att.title + " " + destName)}`;

  let tabBodyHtml = '';

  if (activeModalTab === 'details') {
    const imgs = att.gallery || [att.image];
    tabBodyHtml = `
      <!-- Gallery Carousel -->
      <div class="modal-gallery">
        <img id="modal-gallery-img" src="${imgs[activeModalGalleryIndex] || att.image}" alt="${att.title}">
        ${imgs.length > 1 ? `
          <button class="gallery-nav-btn prev-btn" onclick="prevModalImage()">
            <i data-lucide="chevron-left"></i>
          </button>
          <button class="gallery-nav-btn next-btn" onclick="nextModalImage()">
            <i data-lucide="chevron-right"></i>
          </button>
          <div id="modal-thumb-dots" class="thumb-dots-container">
            ${imgs.map((_, i) => `<div class="dot-thumb ${i === activeModalGalleryIndex ? 'active' : ''}" onclick="setModalImageIndex(${i})"></div>`).join('')}
          </div>
        ` : ''}
      </div>

      <div class="modal-body">
        <p style="font-size:1rem; color:#1E293B; line-height:1.7; margin-bottom:1.5rem;">${att.description}</p>
        
        ${att.highlights ? `
          <div style="margin-bottom:1.5rem;">
            <h4 style="font-size:0.95rem; font-weight:700; color:#0F172A; margin-bottom:0.6rem;">Key Highlights:</h4>
            <div style="display:flex; gap:0.5rem; flex-wrap:wrap;">
              ${att.highlights.map(h => `<span class="badge badge-cyan">✨ ${h}</span>`).join('')}
            </div>
          </div>
        ` : ''}

        ${att.insiderTip ? `
          <div style="background:#FEF3C7; border-left:4px solid #F59E0B; padding:1rem; border-radius:0 12px 12px 0; color:#92400E; font-size:0.9rem;">
            <strong>💡 Local Insider Tip:</strong> ${att.insiderTip}
          </div>
        ` : ''}
      </div>
    `;
  } else if (activeModalTab === 'gmaps') {
    tabBodyHtml = `
      <div style="padding:1.5rem;">
        <div style="margin-bottom:1rem; display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:0.75rem;">
          <div>
            <h3 style="font-size:1.2rem; font-weight:700; color:#0F172A;">Google Maps Interactive View</h3>
            <p style="font-size:0.85rem; color:#64748B;">Address: ${att.address || (att.title + ', ' + destName)}</p>
          </div>
          <div style="display:flex; gap:0.5rem;">
            <a href="${gmapsSearchUrl}" target="_blank" class="btn-primary">
              <i data-lucide="external-link"></i> Full Google Maps
            </a>
            <a href="${gmapsDirectionsUrl}" target="_blank" class="btn-secondary">
              <i data-lucide="navigation"></i> Directions
            </a>
          </div>
        </div>

        <div style="width:100%; height:400px; border-radius:16px; overflow:hidden; border:1px solid #CBD5E1;">
          <iframe 
            width="100%" 
            height="100%" 
            frameborder="0" 
            style="border:0;" 
            src="${gmapsEmbedUrl}" 
            allowfullscreen
          ></iframe>
        </div>
      </div>
    `;
  } else if (activeModalTab === 'ai') {
    tabBodyHtml = `
      <div style="padding:1.5rem;">
        <div style="margin-bottom:1rem;">
          <h3 style="font-size:1.2rem; font-weight:700; color:#0F172A;">Google AI Assistant for ${att.title}</h3>
          <p style="font-size:0.85rem; color:#64748B;">Ask Google AI about visiting hours, photography tips, or nearby places for this spot.</p>
        </div>

        <div class="ai-chat-box" style="height:350px;">
          <div id="modal-ai-messages" class="ai-chat-messages">
            <div class="chat-bubble chat-bubble-ai">
              Ask me anything about <strong>${att.title}</strong> in ${destName}! (e.g. Best time of day to take photos, recommended visiting duration, or nearby cafes).
            </div>
          </div>
          <div class="chat-input-row">
            <input type="text" id="modal-ai-input" placeholder="Ask AI about ${att.title}..." onkeypress="if(event.key === 'Enter') handleModalAiSend('${att.title}', '${destName}')" />
            <button class="btn-ai" onclick="handleModalAiSend('${att.title}', '${destName}')">Send</button>
          </div>
        </div>
      </div>
    `;
  }

  container.innerHTML = `
    <div style="padding:1.5rem 2rem 1rem 2rem; border-bottom:1px solid #E2E8F0; display:flex; justify-content:space-between; align-items:flex-start; flex-wrap:wrap; gap:1rem;">
      <div>
        <div class="badge badge-primary" style="margin-bottom:0.4rem;">${att.category} • ${destName}</div>
        <h2 style="font-size:1.8rem; font-weight:800; color:#0F172A;">${att.title}</h2>
        <div style="font-size:0.88rem; color:#64748B; margin-top:0.25rem; display:flex; gap:1rem;">
          <span>⭐ ${att.rating} rating</span>
          <span>⏱️ ${att.duration}</span>
          <span>🏷️ ${att.price}</span>
        </div>
      </div>

      <button class="btn-secondary" onclick="toggleWishlist('${att.id}')">
        <i data-lucide="heart" style="color:${isWishlisted ? '#DB2777' : '#64748B'}; fill:${isWishlisted ? '#DB2777' : 'none'};"></i>
        ${isWishlisted ? 'Saved' : 'Save Spot'}
      </button>
    </div>

    <!-- Navigation Tabs -->
    <div style="padding:1rem 2rem 0 2rem;">
      <div class="modal-tabs-header">
        <button class="modal-tab-btn ${activeModalTab === 'details' ? 'active' : ''}" onclick="switchModalTab('details')">
          <i data-lucide="image"></i> Photos & Details
        </button>
        <button class="modal-tab-btn ${activeModalTab === 'gmaps' ? 'active' : ''}" onclick="switchModalTab('gmaps')">
          <i data-lucide="map"></i> Google Maps Open View
        </button>
        <button class="modal-tab-btn ${activeModalTab === 'ai' ? 'active' : ''}" onclick="switchModalTab('ai')">
          <i data-lucide="bot"></i> Ask Google AI
        </button>
      </div>
    </div>

    ${tabBodyHtml}
  `;
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

// 7. Google AI Assistant Integration & API Calls
let googleApiKey = localStorage.getItem('google_gemini_key') || '';

function loadSavedApiKey() {
  const input = document.getElementById('google-gemini-key-input');
  if (input && googleApiKey) input.value = googleApiKey;
}

function openApiSettingsModal() {
  loadSavedApiKey();
  document.getElementById('api-settings-modal').style.display = 'flex';
  if (window.lucide) lucide.createIcons();
}

function closeApiSettingsModal() {
  document.getElementById('api-settings-modal').style.display = 'none';
}

function saveGoogleApiKey() {
  const input = document.getElementById('google-gemini-key-input');
  if (input) {
    googleApiKey = input.value.trim();
    localStorage.setItem('google_gemini_key', googleApiKey);
    alert(googleApiKey ? 'Google Gemini API Key saved successfully!' : 'API Key cleared. Keyless Open AI mode enabled.');
    closeApiSettingsModal();
  }
}

function openAiGuideModal() {
  document.getElementById('ai-guide-modal').style.display = 'flex';
  if (window.lucide) lucide.createIcons();
}

function openAiGuideModalForDest(destName) {
  openAiGuideModal();
  sendAiQuickPrompt(`Provide top travel recommendations, local dining tips, and a day plan for ${destName}`);
}

function closeAiGuideModal() {
  document.getElementById('ai-guide-modal').style.display = 'none';
}

function sendAiQuickPrompt(promptText) {
  const input = document.getElementById('ai-chat-input');
  if (input) {
    input.value = promptText;
    handleAiSend();
  }
}

async function handleAiSend() {
  const input = document.getElementById('ai-chat-input');
  const messagesContainer = document.getElementById('ai-chat-messages');
  if (!input || !messagesContainer) return;

  const userQuery = input.value.trim();
  if (!userQuery) return;

  // Render User Bubble
  messagesContainer.innerHTML += `
    <div class="chat-bubble chat-bubble-user">${escapeHtml(userQuery)}</div>
  `;
  input.value = '';
  messagesContainer.scrollTop = messagesContainer.scrollHeight;

  // Render Loading AI Bubble
  const loadingId = 'ai-loading-' + Date.now();
  messagesContainer.innerHTML += `
    <div id="${loadingId}" class="chat-bubble chat-bubble-ai" style="display:flex; align-items:center; gap:0.5rem;">
      <div class="spinner-ring" style="border-top-color:#7C3AED; width:16px; height:16px;"></div>
      <span>Google AI is thinking...</span>
    </div>
  `;
  messagesContainer.scrollTop = messagesContainer.scrollHeight;

  try {
    const aiResponse = await callGoogleGeminiApi(userQuery);
    document.getElementById(loadingId)?.remove();

    messagesContainer.innerHTML += `
      <div class="chat-bubble chat-bubble-ai">${formatAiResponse(aiResponse)}</div>
    `;
  } catch (err) {
    document.getElementById(loadingId)?.remove();
    messagesContainer.innerHTML += `
      <div class="chat-bubble chat-bubble-ai" style="color:#DC2626;">
        Sorry, Google AI service encountered an error. Please try again!
      </div>
    `;
  }

  messagesContainer.scrollTop = messagesContainer.scrollHeight;
}

async function handleModalAiSend(spotTitle, destName) {
  const input = document.getElementById('modal-ai-input');
  const messagesContainer = document.getElementById('modal-ai-messages');
  if (!input || !messagesContainer) return;

  const query = input.value.trim();
  if (!query) return;

  messagesContainer.innerHTML += `<div class="chat-bubble chat-bubble-user">${escapeHtml(query)}</div>`;
  input.value = '';
  messagesContainer.scrollTop = messagesContainer.scrollHeight;

  const fullPrompt = `Regarding ${spotTitle} in ${destName}: ${query}`;

  const loadingId = 'modal-ai-loading-' + Date.now();
  messagesContainer.innerHTML += `
    <div id="${loadingId}" class="chat-bubble chat-bubble-ai" style="display:flex; align-items:center; gap:0.5rem;">
      <div class="spinner-ring" style="border-top-color:#7C3AED; width:16px; height:16px;"></div>
      <span>Google AI thinking...</span>
    </div>
  `;
  messagesContainer.scrollTop = messagesContainer.scrollHeight;

  const response = await callGoogleGeminiApi(fullPrompt);
  document.getElementById(loadingId)?.remove();

  messagesContainer.innerHTML += `<div class="chat-bubble chat-bubble-ai">${formatAiResponse(response)}</div>`;
  messagesContainer.scrollTop = messagesContainer.scrollHeight;
}

// Call Google Gemini API (with seamless fallback generator if key is missing/invalid)
async function callGoogleGeminiApi(promptText) {
  if (googleApiKey) {
    try {
      const endpoint = `https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=${googleApiKey}`;
      const res = await fetch(endpoint, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          contents: [{ parts: [{ text: `You are Wanderlust Google AI Travel Assistant. Provide concise, helpful travel advice for: ${promptText}` }] }]
        })
      });
      const data = await res.json();
      if (data.candidates && data.candidates[0]?.content?.parts[0]?.text) {
        return data.candidates[0].content.parts[0].text;
      }
    } catch (e) {
      console.warn('Google Gemini API fetch error, falling back to smart response generator:', e);
    }
  }

  // Smart Built-in Travel Response Generator
  return generateSmartFallbackAiResponse(promptText);
}

function generateSmartFallbackAiResponse(query) {
  const q = query.toLowerCase();
  
  if (q.includes('paris')) {
    return `<strong>🗼 Google AI Travel Guide for Paris:</strong><br><br>
    • <strong>Must-Visit Spots:</strong> Eiffel Tower (sunset is best), Louvre Museum, Sacré-Cœur in Montmartre, and Musée d'Orsay.<br>
    • <strong>Local Eats:</strong> Fresh croissants at local boulangeries, Duck Confit, and Macarons from Ladurée.<br>
    • <strong>Pro Tip:</strong> Buy a Paris Museum Pass to skip line queues at 50+ attractions!`;
  }
  if (q.includes('tokyo')) {
    return `<strong>⛩️ Google AI Travel Guide for Tokyo:</strong><br><br>
    • <strong>Must-Visit Spots:</strong> Shibuya Crossing, Senso-ji Temple in Asakusa, teamLab Planets, and Meiji Shrine.<br>
    • <strong>Local Eats:</strong> Tonkotsu Ramen in Shinjuku, Tsukiji Outer Market fresh sushi, and Match ice cream.<br>
    • <strong>Pro Tip:</strong> Grab a Suica IC card at the airport for effortless subway rides.`;
  }
  if (q.includes('rome')) {
    return `<strong>🏛️ Google AI Travel Guide for Rome:</strong><br><br>
    • <strong>Must-Visit Spots:</strong> The Colosseum & Forum, Trevi Fountain, Pantheon, and St. Peter's Basilica.<br>
    • <strong>Local Eats:</strong> Spaghetti Carbonara, Suppli (fried rice balls), and authentic Artisanal Gelato.<br>
    • <strong>Pro Tip:</strong> Carry a water bottle—Rome's free street fountains (nasoni) provide ice-cold drinking water!`;
  }
  if (q.includes('photo') || q.includes('picture') || q.includes('camera')) {
    return `<strong>📸 Top Google AI Photo Spot Recommendations:</strong><br><br>
    1. <strong>Eiffel Tower (Paris):</strong> Trocadéro Gardens at sunrise for empty golden hour shots.<br>
    2. <strong>Shibuya Sky (Tokyo):</strong> 360-degree glass rooftop overlooking neon Tokyo.<br>
    3. <strong>Tegalalang Rice Terraces (Bali):</strong> 6:30 AM morning light through jungle mist.<br>
    4. <strong>Hawa Mahal (Jaipur):</strong> Tattoo Cafe rooftop directly facing the pink lattice facade.`;
  }
  if (q.includes('food') || q.includes('eat') || q.includes('dining') || q.includes('cafe')) {
    return `<strong>🍕 Google AI Gourmet Travel Recommendations:</strong><br><br>
    • <strong>Paris:</strong> Fresh croissants & bistro steak frites in Le Marais.<br>
    • <strong>Tokyo:</strong> Memory Lane (Omoide Yokocho) for grilled yakitori skewers.<br>
    • <strong>Jaipur:</strong> Dal Baati Churma and Pyaz Kachori at Rawat Mishtan Bhandar.<br>
    • <strong>Rome:</strong> Trastevere neighborhood for authentic Roman Carbonara and Aperitivo.`;
  }
  if (q.includes('budget') || q.includes('cheap') || q.includes('money')) {
    return `<strong>💡 Google AI Smart Budget Travel Tips:</strong><br><br>
    1. <strong>Free Entry Days:</strong> Many national museums have free entrance on the 1st Sunday of each month.<br>
    2. <strong>Public Transit:</strong> Use daily or weekly travel cards instead of taxis.<br>
    3. <strong>Local Markets:</strong> Eat at food halls and night markets for delicious local food at 1/3 restaurant prices.<br>
    4. <strong>Free Walking Tours:</strong> Join tip-based city walking tours for expert local context.`;
  }

  return `<strong>🌐 Google AI Travel Guide Insights:</strong><br><br>
  Thank you for your inquiry about <em>"${escapeHtml(query)}"</em>!<br><br>
  • <strong>Best Travel Approach:</strong> Plan high-demand landmarks in the early morning to avoid crowds.<br>
  • <strong>Navigation:</strong> Use interactive map markers and Google Maps links on this site for step-by-step directions.<br>
  • <strong>AI Tip:</strong> You can enter a free Google Gemini API Key in the settings menu above for live custom query streaming!`;
}

function formatAiResponse(text) {
  return text.replace(/\n/g, '<br>');
}

function escapeHtml(str) {
  return str.replace(/[&<>"']/g, function(m) {
    return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' }[m];
  });
}

// 8. LocalStorage Wishlist
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
  if (activeModalAttraction && activeModalAttraction.id === attId) {
    let destName = '';
    DESTINATIONS.forEach(d => {
      if (d.attractions.some(a => a.id === attId)) destName = d.name;
    });
    renderModalContent(activeModalAttraction, destName);
  }
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
          <i data-lucide="heart" style="width:44px; height:44px; color:#CBD5E1; margin-bottom:1rem;"></i>
          <p style="font-weight:600; color:#0F172A;">No saved tourist locations yet.</p>
        </div>
      `;
    } else {
      drawerBody.innerHTML = wishlist.map(w => `
        <div style="background:#F8FAFC; border:1px solid #E2E8F0; border-radius:16px; padding:0.85rem; display:flex; gap:0.85rem; align-items:center;">
          <img src="${w.image}" style="width:60px; height:60px; border-radius:12px; object-fit:cover;" />
          <div style="flex:1;">
            <div style="font-size:0.72rem; color:#059669; font-weight:600;">${w.category}</div>
            <div style="font-size:0.92rem; font-weight:700; color:#0F172A; cursor:pointer;" onclick="openAttractionModal('${w.id}')">${w.title}</div>
            <div style="font-size:0.78rem; color:var(--text-muted);">★ ${w.rating} • ${w.price}</div>
          </div>
          <button onclick="toggleWishlist('${w.id}')" style="background:none; border:none; color:#DC2626; cursor:pointer;"><i data-lucide="trash-2" style="width:16px;"></i></button>
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
    <span onclick="selectDestinationById('${d.id}')" style="cursor:pointer; color:var(--text-muted);" onmouseenter="this.style.color='#10B981'" onmouseleave="this.style.color='var(--text-muted)'">
      • ${d.name} Tourist Locations (${d.country})
    </span>
  `).join('');
}
