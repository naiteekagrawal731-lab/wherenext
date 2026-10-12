/**
 * YātrāAI - Client-Side Application Logic
 * Pure Vanilla JavaScript (No frameworks)
 */

document.addEventListener('DOMContentLoaded', () => {
    // DOM Elements
    const form = document.getElementById('travelForm');
    const btnSubmit = document.getElementById('btnSubmit');
    const btnFillDemo = document.getElementById('btnFillDemo');
    const loadingSection = document.getElementById('loadingSection');
    const loadingStepText = document.getElementById('loadingStepText');
    const errorSection = document.getElementById('errorSection');
    const errorTitle = document.getElementById('errorTitle');
    const errorMessage = document.getElementById('errorMessage');
    const errorDetailsList = document.getElementById('errorDetailsList');
    const btnCloseError = document.getElementById('btnCloseError');
    const resultsSection = document.getElementById('resultsSection');
    const resultsUserSummary = document.getElementById('resultsUserSummary');
    const userBudgetDisplay = document.getElementById('userBudgetDisplay');
    const disclaimerText = document.getElementById('disclaimerText');
    const cardsGrid = document.getElementById('cardsGrid');
    const apiStatusBadge = document.getElementById('apiStatusBadge');
    const apiStatusText = document.getElementById('apiStatusText');

    let loadingInterval = null;

    // Check Backend & External API Status on Load
    checkApiStatus();

    // Event Listeners
    btnFillDemo.addEventListener('click', fillDemoPreferences);
    btnCloseError.addEventListener('click', () => { errorSection.hidden = true; });
    form.addEventListener('submit', handleFormSubmit);

    /**
     * Checks server status and API configuration
     */
    async function checkApiStatus() {
        try {
            const res = await fetch('/api/status');
            if (res.ok) {
                const data = await res.json();
                const dot = apiStatusBadge.querySelector('.status-dot');
                if (data.geminiConfigured) {
                    dot.className = 'status-dot active';
                    apiStatusText.textContent = data.placesConfigured 
                        ? 'Gemini & Places: Active' 
                        : 'Gemini: Active | Places: Fallback';
                } else {
                    dot.className = 'status-dot warning';
                    apiStatusText.textContent = 'API Key Config Required';
                }
            } else {
                apiStatusText.textContent = 'Backend Connected';
            }
        } catch (err) {
            apiStatusText.textContent = 'Backend Offline';
        }
    }

    /**
     * Populates form with realistic sample preferences
     */
    function fillDemoPreferences() {
        document.getElementById('startingCity').value = 'Mumbai';
        document.getElementById('travelersCount').value = '2';
        document.getElementById('durationDays').value = '4';
        document.getElementById('budgetType').value = 'TOTAL';
        document.getElementById('budgetAmount').value = '45000';
        document.getElementById('travelStyle').value = 'Mid-range';
        document.getElementById('companions').value = 'Couple';
        document.getElementById('preferredTransportation').value = 'Flexible';
        document.getElementById('accommodationPreference').value = 'Mid-range hotel';

        // Set dates ~30 days in future
        const now = new Date();
        const start = new Date(now.getTime() + 30 * 24 * 60 * 60 * 1000);
        const end = new Date(start.getTime() + 4 * 24 * 60 * 60 * 1000);
        document.getElementById('startDate').value = start.toISOString().split('T')[0];
        document.getElementById('endDate').value = end.toISOString().split('T')[0];

        // Select pace
        const paceInputs = document.querySelectorAll('input[name="travelPace"]');
        paceInputs.forEach(input => { input.checked = (input.value === 'Moderate'); });

        // Select interests
        const demoInterests = ['Beaches & Coastal', 'Heritage & Architecture', 'Food & Culinary Trails'];
        const interestBoxes = document.querySelectorAll('input[name="interests"]');
        interestBoxes.forEach(box => {
            box.checked = demoInterests.includes(box.value);
        });

        clearValidationErrors();
    }

    /**
     * Handles Form Submission
     */
    async function handleFormSubmit(e) {
        e.preventDefault();
        clearValidationErrors();
        errorSection.hidden = true;

        const formData = getFormData();
        if (!validateForm(formData)) {
            return;
        }

        setLoadingState(true);

        try {
            const response = await fetch('/api/recommendations', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(formData)
            });

            if (!response.ok) {
                const errData = await response.json().catch(() => null);
                handleApiError(response.status, errData);
                return;
            }

            const data = await response.json();
            renderRecommendations(data);
        } catch (err) {
            showError('Network Error', 'Unable to reach the server. Please check your connection and ensure the Spring Boot server is running.');
        } finally {
            setLoadingState(false);
        }
    }

    /**
     * Extracts values from form into payload object
     */
    function getFormData() {
        const startingCity = document.getElementById('startingCity').value.trim();
        const startDate = document.getElementById('startDate').value;
        const endDate = document.getElementById('endDate').value;
        const travelersCount = parseInt(document.getElementById('travelersCount').value, 10);
        const durationDays = parseInt(document.getElementById('durationDays').value, 10);
        const budgetType = document.getElementById('budgetType').value;
        const budgetAmount = parseFloat(document.getElementById('budgetAmount').value);
        const travelStyle = document.getElementById('travelStyle').value;
        const companions = document.getElementById('companions').value;
        const preferredTransportation = document.getElementById('preferredTransportation').value;
        const accommodationPreference = document.getElementById('accommodationPreference').value;
        
        const paceEl = document.querySelector('input[name="travelPace"]:checked');
        const travelPace = paceEl ? paceEl.value : 'Moderate';

        const interests = [];
        document.querySelectorAll('input[name="interests"]:checked').forEach(cb => {
            interests.push(cb.value);
        });

        return {
            startingCity,
            startDate,
            endDate,
            travelersCount,
            durationDays,
            budgetType,
            budgetAmount,
            travelStyle,
            companions,
            preferredTransportation,
            accommodationPreference,
            travelPace,
            interests
        };
    }

    /**
     * Validates form inputs before submission
     */
    function validateForm(data) {
        let isValid = true;

        if (!data.startingCity) {
            showFieldError('err-startingCity', 'Starting city is required.');
            isValid = false;
        }

        if (isNaN(data.travelersCount) || data.travelersCount < 1) {
            showFieldError('err-travelersCount', 'Must have at least 1 traveler.');
            isValid = false;
        }

        if (isNaN(data.durationDays) || data.durationDays < 1) {
            showFieldError('err-durationDays', 'Duration must be at least 1 day.');
            isValid = false;
        }

        if (isNaN(data.budgetAmount) || data.budgetAmount <= 0) {
            showFieldError('err-budgetAmount', 'Please enter a valid budget amount in ₹.');
            isValid = false;
        }

        return isValid;
    }

    function showFieldError(id, msg) {
        const el = document.getElementById(id);
        if (el) el.textContent = msg;
    }

    function clearValidationErrors() {
        document.querySelectorAll('.field-error').forEach(el => { el.textContent = ''; });
    }

    /**
     * Toggles Loading animation and step text
     */
    function setLoadingState(isLoading) {
        btnSubmit.disabled = isLoading;
        btnSubmit.querySelector('.btn-text').textContent = isLoading ? 'Analyzing Destinations...' : 'Generate Recommendations & Cost Breakdown';
        btnSubmit.querySelector('.btn-spinner').hidden = !isLoading;
        loadingSection.hidden = !isLoading;

        if (isLoading) {
            resultsSection.hidden = true;
            loadingSection.scrollIntoView({ behavior: 'smooth' });

            let step = 1;
            const steps = [
                { id: 'step1', text: 'Prompting Gemini AI with travel preferences & budget parameters...' },
                { id: 'step2', text: 'Parsing & validating destination-specific INR cost estimates...' },
                { id: 'step3', text: 'Enquiring Google Places API for verified coordinates, ratings & photos...' }
            ];

            updateStepUI(1, steps);
            clearInterval(loadingInterval);
            loadingInterval = setInterval(() => {
                step = (step % 3) + 1;
                updateStepUI(step, steps);
            }, 3000);
        } else {
            clearInterval(loadingInterval);
        }
    }

    function updateStepUI(currentStep, steps) {
        steps.forEach((s, idx) => {
            const el = document.getElementById(s.id);
            if (!el) return;
            const stepNum = idx + 1;
            if (stepNum < currentStep) {
                el.className = 'step-item step-done';
            } else if (stepNum === currentStep) {
                el.className = 'step-item step-active';
                loadingStepText.textContent = s.text;
            } else {
                el.className = 'step-item';
            }
        });
    }

    /**
     * Handles API Errors with descriptive diagnostics
     */
    function handleApiError(statusCode, errData) {
        let title = 'Unable to Generate Recommendations';
        let message = 'An unexpected error occurred while communicating with travel services.';
        let details = [];

        if (errData) {
            if (errData.error === 'VALIDATION_ERROR') {
                title = 'Invalid Input Preferences';
                message = errData.message || 'Please check the highlighted form values.';
                details = errData.details || [];
            } else if (errData.error === 'GEMINI_API_ERROR') {
                title = 'Gemini AI Service Notice';
                message = errData.message;
            } else if (errData.error === 'EXTERNAL_DATA_ERROR') {
                title = 'Data Processing Error';
                message = errData.message;
            } else if (errData.message) {
                message = errData.message;
            }
        } else if (statusCode === 401) {
            title = 'API Key Required';
            message = 'Gemini API key is not configured on the backend server. Please set GEMINI_API_KEY environment variable.';
        } else if (statusCode === 502) {
            title = 'Upstream API Error';
            message = 'Received an error response from the external recommendation or places provider.';
        }

        showError(title, message, details);
    }

    function showError(title, message, details = []) {
        errorTitle.textContent = title;
        errorMessage.textContent = message;

        if (details && details.length > 0) {
            errorDetailsList.hidden = false;
            errorDetailsList.innerHTML = '<strong>Details:</strong><ul>' + 
                details.map(d => `<li>${escapeHtml(d)}</li>`).join('') + '</ul>';
        } else {
            errorDetailsList.hidden = true;
            errorDetailsList.innerHTML = '';
        }

        errorSection.hidden = false;
        errorSection.scrollIntoView({ behavior: 'smooth' });
    }

    /**
     * Renders recommendations and destination cards
     */
    function renderRecommendations(response) {
        const userPrefs = response.userPreferences || {};
        const destinations = response.destinations || [];

        // Summary Bar
        const budgetFormatted = formatInr(userPrefs.budgetAmount);
        const budgetBasisText = userPrefs.budgetType === 'PER_PERSON' ? 'per person' : 'total';
        resultsUserSummary.textContent = `Starting from ${userPrefs.startingCity} • ${userPrefs.durationDays} Days • ${userPrefs.travelersCount} Traveler(s) • ${userPrefs.travelStyle} Style`;
        
        userBudgetDisplay.innerHTML = `Your Target Budget: <strong>₹${budgetFormatted}</strong> <span style="font-size:0.75rem; color:var(--text-muted)">(${budgetBasisText})</span>`;

        // Disclaimer
        if (response.disclaimer) {
            disclaimerText.innerHTML = `<strong>AI Cost Estimate Notice:</strong> ${escapeHtml(response.disclaimer)}`;
        }

        // Render Cards
        cardsGrid.innerHTML = '';
        if (destinations.length === 0) {
            cardsGrid.innerHTML = '<p class="no-results">No destination recommendations returned. Please refine your inputs and try again.</p>';
        } else {
            destinations.forEach((dest, index) => {
                const card = createDestinationCard(dest, userPrefs, index);
                cardsGrid.appendChild(card);
            });
        }

        resultsSection.hidden = false;
        resultsSection.scrollIntoView({ behavior: 'smooth' });
    }

    /**
     * Builds individual destination card element
     */
    function createDestinationCard(dest, userPrefs, index) {
        const card = document.createElement('article');
        card.className = 'dest-card';

        const place = dest.placeDetails || {};
        const breakdown = dest.costBreakdown || {};
        const totalCost = breakdown.totalTripCost || {};
        const perPersonCost = breakdown.perPersonCost || {};

        // Image & Attribution
        const hasPhoto = place.photoAvailable && place.photoUrl;
        const photoSrc = hasPhoto ? place.photoUrl : '/images/fallback-destination.svg';
        const photoNotice = hasPhoto ? '' : (place.photoUnavailableReason || 'Photo unavailable from Google Places');

        let attributionsHtml = '';
        if (place.photoAttributions && place.photoAttributions.length > 0) {
            const attr = place.photoAttributions[0];
            if (attr.authorUri) {
                attributionsHtml = `<div class="photo-attribution-tag">Photo by <a href="${escapeHtml(attr.authorUri)}" target="_blank" rel="noopener">${escapeHtml(attr.authorName || 'Contributor')}</a></div>`;
            } else {
                attributionsHtml = `<div class="photo-attribution-tag">Photo: ${escapeHtml(attr.authorName || 'Google Places')}</div>`;
            }
        }

        // Rating Badge
        let ratingHtml = '';
        if (place.rating) {
            const countStr = place.userRatingCount ? ` (${place.userRatingCount.toLocaleString()})` : '';
            ratingHtml = `<div class="rating-badge">★ ${place.rating.toFixed(1)}<span>${countStr}</span></div>`;
        }

        // Google Maps Link
        const mapsUrl = place.googleMapsUrl || `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(dest.name + ', ' + dest.state + ', India')}`;

        // Budget Comparison Calculation (Without auto-rejecting per requirement #2)
        const userBudget = userPrefs.budgetAmount || 0;
        const isPerPerson = userPrefs.budgetType === 'PER_PERSON';
        const estimatedCompareValue = isPerPerson ? (perPersonCost.typical || 0) : (totalCost.typical || 0);

        let budgetStatusHtml = '';
        if (estimatedCompareValue > 0 && userBudget > 0) {
            const diff = estimatedCompareValue - userBudget;
            if (diff <= 0) {
                const saved = Math.abs(diff);
                budgetStatusHtml = `<span class="budget-status-pill within">Within Budget (₹${formatInr(saved)} buffer)</span>`;
            } else {
                budgetStatusHtml = `<span class="budget-status-pill above">+₹${formatInr(diff)} over target</span>`;
            }
        }

        // Matching Interests Chips
        const interestsHtml = (dest.matchingInterests || []).map(interest => 
            `<span class="tag-badge interest-match">✓ ${escapeHtml(interest)}</span>`
        ).join('');

        // Recommendation Reasons
        let reasonsHtml = '';
        if (dest.recommendationReasons && dest.recommendationReasons.length > 0) {
            reasonsHtml = `
                <div class="reasons-box">
                    <div class="reasons-box-title">Why Gemini Recommends This:</div>
                    <ul>
                        ${dest.recommendationReasons.map(r => `<li>${escapeHtml(r)}</li>`).join('')}
                    </ul>
                </div>
            `;
        }

        // Cost Breakdown Rows
        const costRows = [
            { label: 'Round-trip Transit', item: breakdown.roundTripTransportation },
            { label: 'Accommodation', item: breakdown.accommodation },
            { label: 'Food & Dining', item: breakdown.food },
            { label: 'Local Transport', item: breakdown.localTransportation },
            { label: 'Activities & Entry Fees', item: breakdown.activitiesAndEntryFees },
            { label: 'Miscellaneous & Buffer', item: breakdown.miscellaneous }
        ];

        const breakdownRowsHtml = costRows.map(row => {
            const costVal = row.item && row.item.estimatedCost !== null && row.item.estimatedCost !== undefined 
                ? `₹${formatInr(row.item.estimatedCost)}` 
                : '<span style="color:var(--text-muted)">Variable</span>';
            const notesVal = row.item && row.item.notes ? `<span class="cost-item-notes">${escapeHtml(row.item.notes)}</span>` : '';
            return `
                <tr>
                    <td class="cost-item-name">${row.label}${notesVal}</td>
                    <td class="cost-item-amount">${costVal}</td>
                </tr>
            `;
        }).join('');

        // Itinerary Days
        let itineraryHtml = '';
        if (dest.itinerary && dest.itinerary.length > 0) {
            itineraryHtml = dest.itinerary.map(day => `
                <div class="itinerary-day-card">
                    <div class="itinerary-day-title">Day ${day.day}: ${escapeHtml(day.title || 'Sightseeing')} ${day.estimatedCost ? `<span style="font-weight:normal; color:var(--text-muted)">(Est: ₹${formatInr(day.estimatedCost)})</span>` : ''}</div>
                    ${day.description ? `<p class="itinerary-day-desc">${escapeHtml(day.description)}</p>` : ''}
                    ${day.activities && day.activities.length > 0 ? `
                        <ul class="itinerary-activities-list">
                            ${day.activities.map(act => `<li>${escapeHtml(act)}</li>`).join('')}
                        </ul>
                    ` : ''}
                </div>
            `).join('');
        } else {
            itineraryHtml = '<p style="color:var(--text-muted); font-size:0.8rem">Flexible daily plan based on traveler preferences.</p>';
        }

        // Assumptions & Uncertainties
        let assumptionsHtml = '';
        if (dest.assumptionsAndUncertainties && dest.assumptionsAndUncertainties.length > 0) {
            assumptionsHtml = `
                <ul>
                    ${dest.assumptionsAndUncertainties.map(a => `<li>${escapeHtml(a)}</li>`).join('')}
                </ul>
            `;
        } else {
            assumptionsHtml = '<p style="color:var(--text-muted); font-size:0.75rem">Estimates assume standard booking windows and average seasonality.</p>';
        }

        // Assemble Inner Card HTML
        card.innerHTML = `
            <div class="card-photo-container">
                <img src="${escapeHtml(photoSrc)}" 
                     alt="${escapeHtml(dest.name)}" 
                     class="card-photo" 
                     loading="lazy"
                     onerror="this.onerror=null; this.src='/images/fallback-destination.svg'; this.nextElementSibling.querySelector('.photo-notice-tag').textContent='Photo unavailable from Google Places';">
                
                <div class="photo-overlay-badges">
                    <span class="state-badge">${escapeHtml(dest.state || 'India')}</span>
                    ${ratingHtml}
                </div>

                ${photoNotice ? `<div class="photo-notice-tag">${escapeHtml(photoNotice)}</div>` : ''}
                ${attributionsHtml}
            </div>

            <div class="card-body">
                <div class="dest-header-row">
                    <div>
                        <h4 class="dest-name">${escapeHtml(dest.name)}</h4>
                        ${place.formattedAddress ? `<div class="dest-address">📍 ${escapeHtml(place.formattedAddress)}</div>` : ''}
                    </div>
                    <a href="${escapeHtml(mapsUrl)}" target="_blank" rel="noopener noreferrer" class="maps-link-btn" title="Open Google Maps in new tab">
                        <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"></path>
                            <polyline points="15 3 21 3 21 9"></polyline>
                            <line x1="10" y1="14" x2="21" y2="3"></line>
                        </svg>
                        Maps
                    </a>
                </div>

                <p class="dest-desc">${escapeHtml(dest.description || '')}</p>

                ${interestsHtml ? `<div class="tags-row">${interestsHtml}</div>` : ''}

                ${reasonsHtml}

                <!-- Budget Comparison Box -->
                <div class="budget-comparison-box">
                    <div class="budget-comp-col">
                        <span class="budget-comp-label">Your ${isPerPerson ? 'Per-Person' : 'Group'} Budget</span>
                        <span class="budget-comp-val">₹${formatInr(userBudget)}</span>
                    </div>
                    <div class="budget-comp-col" style="text-align:right;">
                        <span class="budget-comp-label">Gemini Estimate</span>
                        <span class="budget-comp-val">₹${formatInr(estimatedCompareValue)}</span>
                    </div>
                    ${budgetStatusHtml}
                </div>

                <!-- Cost Breakdown Table -->
                <div class="cost-breakdown-section">
                    <div class="cost-section-title">
                        <span>Cost Breakdown (INR)</span>
                        <span style="font-size:0.72rem; font-weight:500; color:var(--text-muted);">Gemini AI Estimate</span>
                    </div>
                    <table class="cost-table">
                        <tbody>
                            ${breakdownRowsHtml}
                        </tbody>
                    </table>

                    <!-- Total Price Highlight Box -->
                    <div class="total-cost-highlight">
                        <div class="total-cost-header">
                            <span class="total-cost-title">Total Estimated Trip Cost</span>
                            <span class="total-typical-amount">₹${formatInr(totalCost.typical || 0)}</span>
                        </div>
                        <div class="cost-range-row">
                            <span>Range: ₹${formatInr(totalCost.low || 0)} – ₹${formatInr(totalCost.high || 0)}</span>
                            <span>${totalCost.notes ? escapeHtml(totalCost.notes) : ''}</span>
                        </div>
                        <div class="per-person-line">
                            Per-person estimate: <strong>₹${formatInr(perPersonCost.typical || 0)}</strong> 
                            ${perPersonCost.low && perPersonCost.high ? `(Range: ₹${formatInr(perPersonCost.low)} – ₹${formatInr(perPersonCost.high)})` : ''}
                        </div>
                    </div>
                </div>

                <!-- Collapsible Itinerary Accordion -->
                <button type="button" class="accordion-toggle" data-target="itinerary-${index}">
                    <span>Suggested Itinerary (${dest.itinerary ? dest.itinerary.length : userPrefs.durationDays} Days)</span>
                    <span class="accordion-arrow">▼</span>
                </button>
                <div class="accordion-content" id="itinerary-${index}" hidden>
                    ${itineraryHtml}
                </div>

                <!-- Collapsible Assumptions Accordion -->
                <button type="button" class="accordion-toggle" data-target="assumptions-${index}">
                    <span>Cost Assumptions &amp; Uncertainties</span>
                    <span class="accordion-arrow">▼</span>
                </button>
                <div class="accordion-content assumptions-box" id="assumptions-${index}" hidden>
                    ${assumptionsHtml}
                </div>
            </div>
        `;

        // Wire accordion buttons
        card.querySelectorAll('.accordion-toggle').forEach(btn => {
            btn.addEventListener('click', () => {
                const targetId = btn.getAttribute('data-target');
                const content = card.querySelector('#' + targetId);
                const arrow = btn.querySelector('.accordion-arrow');
                if (content) {
                    content.hidden = !content.hidden;
                    arrow.textContent = content.hidden ? '▼' : '▲';
                }
            });
        });

        return card;
    }

    /**
     * Number formatting utility for Indian Currency (INR)
     */
    function formatInr(val) {
        if (val === null || val === undefined || isNaN(val)) return '0';
        return Number(val).toLocaleString('en-IN', { maximumFractionDigits: 0 });
    }

    /**
     * Simple HTML escape to prevent XSS
     */
    function escapeHtml(text) {
        if (!text) return '';
        return String(text)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    }
});
