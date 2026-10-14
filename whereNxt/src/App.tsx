import { useMemo, useState } from "react";

type IconProps = { name: string; filled?: boolean; className?: string };

function Icon({ name, filled = false, className = "" }: IconProps) {
  return (
    <span
      aria-hidden="true"
      className={`material-symbols-rounded ${filled ? "filled" : ""} ${className}`}
    >
      {name}
    </span>
  );
}

export type CostRange = {
  low: number;
  typical: number;
  high: number;
  currency: string;
};

export type CostEstimates = {
  transport?: CostRange;
  accommodation?: CostRange;
  food?: CostRange;
  localTravel?: CostRange;
  activities?: CostRange;
  miscellaneous?: CostRange;
  total?: CostRange;
};

export type Destination = {
  id: number;
  place: string;
  state: string;
  match: number;
  duration: string;
  cost: string;
  costNum: number;
  image: string;
  credit?: string;
  handle?: string;
  sourceUrl?: string;
  license?: string;
  licenseUrl?: string;
  tags: string[];
  summary: string;
  costs: number[];
  itinerary: [string, string, string][];
  costEstimates?: CostEstimates;
  activities?: string[];
  travelTips?: string[];
  assumptions?: string[];
  uncertaintyMarkers?: string[];
};

function cleanImageUrl(url: string): string {
  if (!url) return "";
  let cleaned = url.trim();
  const qIdx = cleaned.indexOf("?");
  if (qIdx !== -1) cleaned = cleaned.substring(0, qIdx);
  if (cleaned.includes("/commons/thumb/")) {
    cleaned = cleaned.replace("/commons/thumb/", "/commons/");
    const lastSlash = cleaned.lastIndexOf("/");
    if (lastSlash !== -1 && /\/\d+px-.*/.test(cleaned.substring(lastSlash))) {
      cleaned = cleaned.substring(0, lastSlash);
    }
  }
  if (cleaned.startsWith("https://thumb.wikimedia.org/")) {
    cleaned = "https://upload.wikimedia.org/" + cleaned.substring("https://thumb.wikimedia.org/".length());
  } else if (cleaned.startsWith("http://thumb.wikimedia.org/")) {
    cleaned = "https://upload.wikimedia.org/" + cleaned.substring("http://thumb.wikimedia.org/".length());
  }
  return cleaned;
}

function TravelImage({
  src,
  alt,
  className = "",
}: {
  src: string;
  alt: string;
  className?: string;
}) {
  const [currentSrc, setCurrentSrc] = useState(() => cleanImageUrl(src));
  const [attemptProxy, setAttemptProxy] = useState(false);
  const [attemptFallback, setAttemptFallback] = useState(false);

  const cleanedSrc = cleanImageUrl(src);
  if (currentSrc !== cleanedSrc && !attemptProxy && !attemptFallback) {
    setCurrentSrc(cleanedSrc);
  }

  const handleError = () => {
    if (!attemptProxy && cleanedSrc) {
      setAttemptProxy(true);
      setCurrentSrc(`/api/images/proxy?url=${encodeURIComponent(cleanedSrc)}`);
    } else if (!attemptFallback) {
      setAttemptFallback(true);
      setCurrentSrc("https://upload.wikimedia.org/wikipedia/commons/1/1d/Taj_Mahal_%28Edited%29.jpeg");
    }
  };

  return (
    <img
      src={currentSrc}
      alt={alt}
      className={className}
      referrerPolicy="no-referrer"
      loading="lazy"
      onError={handleError}
    />
  );
}

const initialDestinations: Destination[] = [
  {
    id: 1,
    place: "Jaipur",
    state: "Rajasthan",
    match: 96,
    duration: "4 days",
    cost: "₹24,500",
    costNum: 24500,
    image:
      "https://upload.wikimedia.org/wikipedia/commons/f/f6/Hawa_Mahal_%28The_Palace_of_Winds%29_in_Jaipur%2C_20191218_1201_9174.jpg",
    credit: "Jakub Halun",
    handle: "CC BY-SA 4.0",
    sourceUrl: "https://commons.wikimedia.org/wiki/File:Hawa_Mahal_(The_Palace_of_Winds)_in_Jaipur,_20191218_1201_9174.jpg",
    license: "CC BY-SA 4.0",
    licenseUrl: "https://creativecommons.org/licenses/by-sa/4.0",
    tags: ["Heritage", "Culinary"],
    summary:
      "Rose-hued palaces, artisan bazaars and slow rooftop dinners—balanced into one vibrant long weekend.",
    costs: [22, 28, 32, 18],
    itinerary: [
      ["Day 1", "Old City & royal stories", "Hawa Mahal at first light, City Palace courtyards and a guided spice walk."],
      ["Day 2", "Forts above the city", "Amber Fort, stepwells and sunset from Nahargarh with a local dinner."],
      ["Day 3", "Craft, colour & cuisine", "Block-print workshop, bazaars and an intimate Rajasthani tasting menu."],
    ],
    travelTips: ["Visit Hawa Mahal early morning for soft photography light", "Try the traditional pyaaz kachori in Johari Bazaar"],
  },
  {
    id: 2,
    place: "Alappuzha",
    state: "Kerala",
    match: 93,
    duration: "5 days",
    cost: "₹29,800",
    costNum: 29800,
    image:
      "https://upload.wikimedia.org/wikipedia/commons/e/e4/Alappuzha_Boat_Beauty_W.jpg",
    credit: "Sivahari",
    handle: "CC BY-SA 4.0",
    sourceUrl: "https://commons.wikimedia.org/wiki/File:Alappuzha_Boat_Beauty_W.jpg",
    license: "CC BY-SA 4.0",
    licenseUrl: "https://creativecommons.org/licenses/by-sa/4.0",
    tags: ["Wellness", "Nature"],
    summary:
      "Quiet backwaters, coconut-lined canals and restorative coastal flavours at a beautifully unhurried pace.",
    costs: [18, 22, 42, 18],
    itinerary: [
      ["Day 1", "Arrive by the water", "Check into a canal-side stay, cycle the village lanes and watch sunset by canoe."],
      ["Day 2", "Backwater immersion", "A responsible houseboat journey with a home-cooked Kerala lunch."],
      ["Day 3", "Coast & calm", "Marari beach, Ayurvedic massage and a seafood dinner by the shore."],
    ],
    travelTips: ["Opt for a sunset canoe through narrower interior canals", "Savor an authentic banana leaf sadhya lunch"],
  },
  {
    id: 3,
    place: "Varanasi",
    state: "Uttar Pradesh",
    match: 89,
    duration: "3 days",
    cost: "₹18,200",
    costNum: 18200,
    image:
      "https://upload.wikimedia.org/wikipedia/commons/0/04/Ahilya_Ghat_by_the_Ganges%2C_Varanasi.jpg",
    credit: "Firoze Edassery",
    handle: "CC BY-SA 3.0",
    sourceUrl: "https://commons.wikimedia.org/wiki/File:Ahilya_Ghat_by_the_Ganges,_Varanasi.jpg",
    license: "CC BY-SA 3.0",
    licenseUrl: "https://creativecommons.org/licenses/by-sa/3.0",
    tags: ["Spiritual", "Culture"],
    summary:
      "Ancient lanes, sunrise on the Ganges and living traditions, thoughtfully guided for a meaningful city break.",
    costs: [25, 19, 26, 30],
    itinerary: [
      ["Day 1", "The river awakens", "Sunrise boat ride, breakfast in the old city and a walk through hidden temples."],
      ["Day 2", "Stories & ceremonies", "Silk weaving studio, Sarnath and evening Ganga Aarti from a quiet viewpoint."],
      ["Day 3", "Local rhythms", "Market breakfast, music workshop and a final walk along the southern ghats."],
    ],
    travelTips: ["Board a wooden rowboat at dawn for the quietest ghat atmosphere", "Sample freshly churned malaiyo in Thatheri Bazar"],
  },
];

const interests = ["Adventure", "Heritage", "Culinary", "Nature", "Wellness", "Spiritual"];
export type CityOption = {
  name: string;
  state: string;
  hubType?: string;
};

export type DestinationOption = {
  name: string;
  state: string;
  tag: string;
  vibe: string;
};

export const startingCityOptions: CityOption[] = [
  { name: "Bengaluru", state: "Karnataka", hubType: "Major Airport / Tech Hub" },
  { name: "Mumbai", state: "Maharashtra", hubType: "Financial Capital / Western Hub" },
  { name: "New Delhi", state: "Delhi NCR", hubType: "National Capital / Northern Hub" },
  { name: "Jabalpur", state: "Madhya Pradesh", hubType: "Central India / Cultural Gateway" },
  { name: "Bhopal", state: "Madhya Pradesh", hubType: "Lake City / Central Hub" },
  { name: "Indore", state: "Madhya Pradesh", hubType: "Cleanest City / Food Hub" },
  { name: "Hyderabad", state: "Telangana", hubType: "Deccan Transit Hub" },
  { name: "Chennai", state: "Tamil Nadu", hubType: "Southern Coastal Hub" },
  { name: "Kolkata", state: "West Bengal", hubType: "Eastern Gateway" },
  { name: "Pune", state: "Maharashtra", hubType: "Western Ghats Gateway" },
  { name: "Ahmedabad", state: "Gujarat", hubType: "Heritage City Hub" },
  { name: "Jaipur", state: "Rajasthan", hubType: "Pink City Airport" },
  { name: "Chandigarh", state: "Punjab / Haryana", hubType: "Gateway to the Himalayas" },
  { name: "Lucknow", state: "Uttar Pradesh", hubType: "City of Nawabs" },
  { name: "Kochi", state: "Kerala", hubType: "Malabar Coastal Hub" },
  { name: "Varanasi", state: "Uttar Pradesh", hubType: "Sacred Eastern Hub" },
  { name: "Goa (Dabolim/Mopa)", state: "Goa", hubType: "Coastal Airport Hub" },
  { name: "Dehradun", state: "Uttarakhand", hubType: "Foothills of Garhwal" },
  { name: "Amritsar", state: "Punjab", hubType: "Holy City Airport" },
  { name: "Nagpur", state: "Maharashtra", hubType: "Zero Mile Central India" },
  { name: "Patna", state: "Bihar", hubType: "Gangetic Plains Hub" },
  { name: "Guwahati", state: "Assam", hubType: "Northeast Gateway" },
  { name: "Srinagar", state: "Jammu & Kashmir", hubType: "Kashmir Valley Hub" },
];

export const availableDestinations: DestinationOption[] = [
  { name: "Goa", state: "Goa", tag: "Beaches", vibe: "Sun-washed beaches, colonial churches & vibrant coastal shacks" },
  { name: "Manali", state: "Himachal Pradesh", tag: "Mountains", vibe: "Snow-peaked valleys, Solang adventures & cedar trails" },
  { name: "Jaipur", state: "Rajasthan", tag: "Heritage", vibe: "Rose-hued palaces, Amber Fort & bustling bazaars" },
  { name: "Udaipur", state: "Rajasthan", tag: "Heritage", vibe: "Lake Pichola sunsets, floating palaces & royal havelis" },
  { name: "Jaisalmer", state: "Rajasthan", tag: "Desert", vibe: "Golden sandstone fort, Thar Desert dunes & starlit glamping" },
  { name: "Alappuzha (Alleppey)", state: "Kerala", tag: "Backwaters", vibe: "Serene backwater lagoons, houseboats & coconut groves" },
  { name: "Munnar", state: "Kerala", tag: "Hill Station", vibe: "Misty rolling tea plantations, cool waterfalls & cloud walks" },
  { name: "Srinagar & Kashmir", state: "Jammu & Kashmir", tag: "Himalayas", vibe: "Dal Lake wooden shikaras, floating markets & Mughal gardens" },
  { name: "Ladakh (Leh)", state: "Ladakh", tag: "Adventure", vibe: "Pangong Tso blue waters, high passes & cliffside monasteries" },
  { name: "Rishikesh", state: "Uttarakhand", tag: "Spiritual & Adventure", vibe: "Sacred Ganga aarti, yoga retreats & thrilling river rafting" },
  { name: "Varanasi", state: "Uttar Pradesh", tag: "Spiritual", vibe: "Timeless river ghats, morning boat chants & evening aarti" },
  { name: "Agra", state: "Uttar Pradesh", tag: "Heritage", vibe: "Taj Mahal ivory grandeur, Agra Fort & Mughal gardens" },
  { name: "Ooty", state: "Tamil Nadu", tag: "Hill Station", vibe: "Heritage Nilgiri toy train, botanical gardens & tea estates" },
  { name: "Pondicherry", state: "Puducherry", tag: "Coastal & Heritage", vibe: "French colonial White Town, serene promenade & Auroville" },
  { name: "Hampi", state: "Karnataka", tag: "Heritage", vibe: "Ancient Vijayanagara boulder ruins, temples & Tungabhadra sunsets" },
  { name: "Gokarna", state: "Karnataka", tag: "Beaches", vibe: "Pristine Om Beach, peaceful coastal treks & temple town vibe" },
  { name: "Coorg (Kodagu)", state: "Karnataka", tag: "Nature", vibe: "Fragrant coffee hills, Abbey Falls & misty Western Ghats" },
  { name: "Amritsar", state: "Punjab", tag: "Spiritual & Food", vibe: "Illuminated Golden Temple, serene sarovar & rich Punjabi flavours" },
  { name: "Darjeeling", state: "West Bengal", tag: "Hill Station", vibe: "Tiger Hill Kanchenjunga sunrise, vintage toy train & tea gardens" },
  { name: "Shimla", state: "Himachal Pradesh", tag: "Hill Station", vibe: "Colonial Mall Road, Christ Church & Himalayan panoramic ridge" },
  { name: "Meghalaya (Cherrapunji)", state: "Meghalaya", tag: "Nature & Adventure", vibe: "Living root bridges, crystal-clear Dawki & plunging waterfalls" },
  { name: "Jabalpur", state: "Madhya Pradesh", tag: "Nature & River", vibe: "Dhuandhar Falls, Bhedaghat Marble Rocks & holy Narmada ghats" },
  { name: "Pachmarhi", state: "Madhya Pradesh", tag: "Hill Station", vibe: "Queen of Satpura, Bee Falls, Dhoopgarh sunset & sacred caves" },
];


function Counter({
  label,
  value,
  setValue,
  suffix,
}: {
  label: string;
  value: number;
  setValue: (value: number) => void;
  suffix: string;
}) {
  return (
    <div className="counter">
      <div>
        <span className="field-label">{label}</span>
        <strong>
          {value} {suffix}
        </strong>
      </div>
      <div className="counter-actions">
        <button
          className="icon-button small"
          aria-label={`Decrease ${label}`}
          onClick={() => setValue(Math.max(1, value - 1))}
        >
          <Icon name="remove" />
        </button>
        <button
          className="icon-button small dark"
          aria-label={`Increase ${label}`}
          onClick={() => setValue(value + 1)}
        >
          <Icon name="add" />
        </button>
      </div>
    </div>
  );
}

function Planner({ onRecommendations }: { onRecommendations: (destinations: Destination[]) => void }) {
  const [step, setStep] = useState(1);
  const [city, setCity] = useState("Bengaluru");
  const [destinationPref, setDestinationPref] = useState("");
  const [budget, setBudget] = useState(45000);
  const [groupBudget, setGroupBudget] = useState(true);
  const [travelers, setTravelers] = useState(2);
  const [days, setDays] = useState(5);
  const [selectedInterests, setSelectedInterests] = useState(["Heritage", "Culinary"]);
  const [pace, setPace] = useState("Balanced");
  const [transport, setTransport] = useState("Train + local");
  const [stayStyle, setStayStyle] = useState("Boutique hotels");
  const [companion, setCompanion] = useState("Partner");
  const [notes, setNotes] = useState("");
  const [showCitySuggestions, setShowCitySuggestions] = useState(false);
  const [showDestSuggestions, setShowDestSuggestions] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const filteredCities = useMemo(() => {
    const q = city.trim().toLowerCase();
    if (!q) return startingCityOptions;
    return startingCityOptions.filter(
      (item) =>
        item.name.toLowerCase().includes(q) ||
        item.state.toLowerCase().includes(q) ||
        (item.hubType && item.hubType.toLowerCase().includes(q)),
    );
  }, [city]);

  const filteredDestinations = useMemo(() => {
    const q = destinationPref.trim().toLowerCase();
    if (!q) return availableDestinations;
    return availableDestinations.filter(
      (dest) =>
        dest.name.toLowerCase().includes(q) ||
        dest.state.toLowerCase().includes(q) ||
        dest.tag.toLowerCase().includes(q) ||
        dest.vibe.toLowerCase().includes(q),
    );
  }, [destinationPref]);

  const toggleInterest = (interest: string) => {
    setSelectedInterests((current) =>
      current.includes(interest)
        ? current.filter((item) => item !== interest)
        : [...current, interest],
    );
  };

  const findPlaces = async () => {
    setLoading(true);
    setErrorMessage(null);
    try {
      const response = await fetch("/api/recommendations", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          startingCity: city,
          destinationPreference: destinationPref,
          budget,
          groupBudget,
          travelers,
          tripLengthDays: days,
          interests: selectedInterests,
          pace,
          transport,
          stayStyle,
          companion,
          notes,
        }),
      });

      if (!response.ok) {
        const errData = await response.json().catch(() => ({}));
        throw new Error(errData.message || `Server responded with ${response.status}`);
      }

      const data = await response.json();
      if (data.destinations && data.destinations.length > 0) {
        onRecommendations(data.destinations);
      }
      document.getElementById("results")?.scrollIntoView({ behavior: "smooth" });
    } catch (err: any) {
      console.warn("Backend request note:", err.message);
      setErrorMessage("Could not reach backend recommendations. Showing current destinations.");
      document.getElementById("results")?.scrollIntoView({ behavior: "smooth" });
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="planner-card" id="planner" aria-label="Trip preference planner">
      <div className="planner-top">
        <div>
          <span className="eyebrow">
            <Icon name="auto_awesome" filled /> AI trip match
          </span>
          <h2>Build your perfect escape</h2>
        </div>
        <span className="step-count">0{step} / 03</span>
      </div>

      <div className="progress-wrap" aria-label={`Step ${step} of 3`}>
        {[1, 2, 3].map((item) => (
          <div className="progress-item" key={item}>
            <button
              className={`progress-node ${step >= item ? "active" : ""}`}
              onClick={() => setStep(item)}
              aria-label={`Go to step ${item}`}
            >
              {step > item ? <Icon name="check" /> : item}
            </button>
            <span>{["Basics", "Your vibe", "Fine tune"][item - 1]}</span>
          </div>
        ))}
        <div className="progress-line">
          <span style={{ width: `${((step - 1) / 2) * 100}%` }} />
        </div>
      </div>

      <div className="step-viewport">
        {step === 1 && (
          <div className="step-panel">
            <div className="field city-field">
              <label htmlFor="city">
                Starting city
                <small style={{ fontWeight: 400, color: "var(--muted)", marginLeft: "6px" }}>
                  (Origin hub)
                </small>
              </label>
              <div className="input-with-icon">
                <Icon name="location_on" />
                <input
                  id="city"
                  value={city}
                  autoComplete="off"
                  onFocus={() => setShowCitySuggestions(true)}
                  onChange={(event) => {
                    setCity(event.target.value);
                    setShowCitySuggestions(true);
                  }}
                  onBlur={() => window.setTimeout(() => setShowCitySuggestions(false), 200)}
                  placeholder="Select or type your starting city (e.g. Jabalpur, Mumbai, Bengaluru)"
                />
                {city ? (
                  <button
                    type="button"
                    className="field-clear"
                    title="Clear starting city"
                    onMouseDown={(e) => {
                      e.preventDefault();
                      setCity("");
                      setShowCitySuggestions(true);
                    }}
                  >
                    <Icon name="close" />
                  </button>
                ) : (
                  <button
                    type="button"
                    className="field-clear"
                    title="Browse starting cities"
                    onMouseDown={(e) => {
                      e.preventDefault();
                      setShowCitySuggestions(!showCitySuggestions);
                    }}
                  >
                    <Icon name="expand_more" />
                  </button>
                )}
              </div>

              {showCitySuggestions && (
                <div className="suggestions">
                  <div className="suggestions-header">
                    <span>Available departure hubs ({filteredCities.length})</span>
                    {city && (
                      <button
                        type="button"
                        className="suggestions-reset-link"
                        onMouseDown={(e) => {
                          e.preventDefault();
                          setCity("");
                        }}
                      >
                        Show all
                      </button>
                    )}
                  </div>
                  {filteredCities.length > 0 ? (
                    filteredCities.map((item) => (
                      <button
                        key={item.name}
                        type="button"
                        className="suggestion-item"
                        onMouseDown={(e) => {
                          e.preventDefault();
                          setCity(item.name);
                          setShowCitySuggestions(false);
                        }}
                      >
                        <div className="suggestion-main">
                          <Icon name="location_city" />
                          <div className="suggestion-text">
                            <span className="suggestion-title">{item.name}</span>
                            <span className="suggestion-subtitle">{item.state}</span>
                          </div>
                        </div>
                        {item.hubType && <span className="suggestion-badge">{item.hubType}</span>}
                      </button>
                    ))
                  ) : (
                    <div className="suggestions-empty">
                      <span>No exact hub match for "{city}". You can still use it as your origin!</span>
                    </div>
                  )}
                </div>
              )}

              <div className="quick-picks">
                <span>Popular origins:</span>
                {["Jabalpur", "Bengaluru", "Mumbai", "New Delhi", "Hyderabad", "Kolkata", "Bhopal", "Indore"].map((c) => (
                  <button
                    key={c}
                    type="button"
                    className={`quick-pick-pill ${city.toLowerCase() === c.toLowerCase() ? "active" : ""}`}
                    onClick={() => setCity(c)}
                  >
                    {c}
                  </button>
                ))}
              </div>
            </div>

            <div className="field" style={{ marginTop: "18px" }}>
              <label htmlFor="destPref">
                Where do you want to go?{" "}
                <small style={{ fontWeight: 400, color: "var(--muted)" }}>
                  (Select available destinations below or leave blank for anywhere)
                </small>
              </label>
              <div className="input-with-icon">
                <Icon name="travel_explore" />
                <input
                  id="destPref"
                  value={destinationPref}
                  autoComplete="off"
                  onFocus={() => setShowDestSuggestions(true)}
                  onChange={(event) => {
                    setDestinationPref(event.target.value);
                    setShowDestSuggestions(true);
                  }}
                  onBlur={() => window.setTimeout(() => setShowDestSuggestions(false), 200)}
                  placeholder="Select a place to visit (e.g. Goa, Manali, Rajasthan, Jabalpur)"
                />
                {destinationPref ? (
                  <button
                    type="button"
                    className="field-clear"
                    title="Clear destination preference"
                    onMouseDown={(e) => {
                      e.preventDefault();
                      setDestinationPref("");
                      setShowDestSuggestions(true);
                    }}
                  >
                    <Icon name="close" />
                  </button>
                ) : (
                  <button
                    type="button"
                    className="field-clear"
                    title="Browse destinations catalog"
                    onMouseDown={(e) => {
                      e.preventDefault();
                      setShowDestSuggestions(!showDestSuggestions);
                    }}
                  >
                    <Icon name="expand_more" />
                  </button>
                )}
              </div>

              {showDestSuggestions && (
                <div className="suggestions">
                  <div className="suggestions-header">
                    <span>Available destinations to visit ({filteredDestinations.length})</span>
                    {destinationPref && (
                      <button
                        type="button"
                        className="suggestions-reset-link"
                        onMouseDown={(e) => {
                          e.preventDefault();
                          setDestinationPref("");
                        }}
                      >
                        Show all
                      </button>
                    )}
                  </div>
                  {filteredDestinations.length > 0 ? (
                    filteredDestinations.map((dest) => (
                      <button
                        key={dest.name}
                        type="button"
                        className="suggestion-item"
                        onMouseDown={(e) => {
                          e.preventDefault();
                          setDestinationPref(dest.name);
                          setShowDestSuggestions(false);
                        }}
                      >
                        <div className="suggestion-main">
                          <Icon name="place" />
                          <div className="suggestion-text">
                            <span className="suggestion-title">
                              {dest.name} <small style={{ color: "var(--muted)", fontWeight: 500 }}>· {dest.state}</small>
                            </span>
                            <span className="suggestion-subtitle">{dest.vibe}</span>
                          </div>
                        </div>
                        <span className="suggestion-badge">{dest.tag}</span>
                      </button>
                    ))
                  ) : (
                    <div className="suggestions-empty">
                      <span>No direct catalog match for "{destinationPref}". Our AI planner will still custom-curate it!</span>
                    </div>
                  )}
                </div>
              )}

              <div className="quick-picks">
                <span>Popular:</span>
                {["Goa", "Manali", "Rajasthan", "Kerala", "Kashmir", "Ladakh", "Agra", "Ooty", "Jabalpur", "Anywhere"].map((d) => (
                  <button
                    key={d}
                    type="button"
                    className={`quick-pick-pill ${
                      (d === "Anywhere" && !destinationPref) ||
                      (d !== "Anywhere" && destinationPref.toLowerCase() === d.toLowerCase())
                        ? "active"
                        : ""
                    }`}
                    onClick={() => setDestinationPref(d === "Anywhere" ? "" : d)}
                  >
                    {d === "Anywhere" ? "✨ Anywhere in India" : d}
                  </button>
                ))}
              </div>
            </div>

            <div className="budget-head">
              <div>
                <label htmlFor="budget">Comfortable budget</label>
                <p>We’ll stay under this amount.</p>
              </div>
              <div className="currency-input">
                <span>₹</span>
                <input
                  aria-label="Budget in rupees"
                  type="number"
                  value={budget}
                  onChange={(event) => setBudget(Number(event.target.value))}
                />
              </div>
            </div>
            <input
              id="budget"
              className="range"
              type="range"
              min="10000"
              max="100000"
              step="1000"
              value={budget}
              onChange={(event) => setBudget(Number(event.target.value))}
              style={{ "--value": `${((budget - 10000) / 90000) * 100}%` } as React.CSSProperties}
            />
            <div className="range-labels">
              <span>₹10k</span>
              <span>₹1L+</span>
            </div>

            <button
              className="switch-row"
              role="switch"
              aria-checked={groupBudget}
              onClick={() => setGroupBudget(!groupBudget)}
            >
              <span>
                <strong>Total group budget</strong>
                <small>Switch off for per-person planning</small>
              </span>
              <i className={groupBudget ? "on" : ""}>
                <b />
              </i>
            </button>

            <div className="counter-grid">
              <Counter label="Travelers" value={travelers} setValue={setTravelers} suffix="people" />
              <Counter label="Trip length" value={days} setValue={setDays} suffix="days" />
            </div>
          </div>
        )}

        {step === 2 && (
          <div className="step-panel">
            <div className="section-prompt">
              <span className="prompt-icon"><Icon name="interests" /></span>
              <div>
                <h3>What calls to you?</h3>
                <p>Choose at least two. Mix and match as much as you like.</p>
              </div>
            </div>
            <div className="chips">
              {interests.map((interest) => {
                const selected = selectedInterests.includes(interest);
                return (
                  <button
                    className={`chip ${selected ? "selected" : ""}`}
                    key={interest}
                    onClick={() => toggleInterest(interest)}
                  >
                    <Icon
                      name={{
                        Adventure: "hiking",
                        Heritage: "account_balance",
                        Culinary: "restaurant",
                        Nature: "forest",
                        Wellness: "spa",
                        Spiritual: "self_improvement",
                      }[interest] || "circle"}
                    />
                    {interest}
                    {selected && <Icon name="check" />}
                  </button>
                );
              })}
            </div>
            <div className="select-grid">
              <label className="select-field" htmlFor="pace">
                <span>Travel pace</span>
                <select id="pace" value={pace} onChange={(e) => setPace(e.target.value)}>
                  <option value="Balanced">Balanced</option>
                  <option value="Relaxed">Relaxed</option>
                  <option value="Fast-paced">Fast-paced</option>
                </select>
                <Icon name="expand_more" />
              </label>
              <label className="select-field" htmlFor="transport">
                <span>Getting around</span>
                <select id="transport" value={transport} onChange={(e) => setTransport(e.target.value)}>
                  <option value="Train + local">Train + local</option>
                  <option value="Flight + private cab">Flight + private cab</option>
                  <option value="Bus + local">Bus + local</option>
                  <option value="Flexible">Flexible</option>
                </select>
                <Icon name="expand_more" />
              </label>
              <label className="select-field" htmlFor="stay">
                <span>Stay style</span>
                <select id="stay" value={stayStyle} onChange={(e) => setStayStyle(e.target.value)}>
                  <option value="Boutique hotels">Boutique hotels</option>
                  <option value="Heritage havelis">Heritage havelis</option>
                  <option value="Homestays">Homestays</option>
                  <option value="Luxury resorts">Luxury resorts</option>
                </select>
                <Icon name="expand_more" />
              </label>
              <label className="select-field" htmlFor="company">
                <span>Travelling with</span>
                <select id="company" value={companion} onChange={(e) => setCompanion(e.target.value)}>
                  <option value="Partner">Partner</option>
                  <option value="Solo">Solo</option>
                  <option value="Family">Family</option>
                  <option value="Friends">Friends</option>
                </select>
                <Icon name="expand_more" />
              </label>
            </div>
          </div>
        )}

        {step === 3 && (
          <div className="step-panel">
            <div className="section-prompt">
              <span className="prompt-icon"><Icon name="tune" /></span>
              <div>
                <h3>Make it truly yours</h3>
                <p>A few final details help us recommend more confidently.</p>
              </div>
            </div>
            <label className="textarea-field" htmlFor="notes">
              <span>Dietary, accessibility or other preferences</span>
              <textarea
                id="notes"
                value={notes}
                maxLength={180}
                onChange={(event) => setNotes(event.target.value)}
                placeholder="e.g. Vegetarian food, minimal stairs, quiet stays..."
              />
              <small>{notes.length} / 180</small>
            </label>
            <div className="info-banner">
              <Icon name="shield" filled />
              <div>
                <strong>Your preferences stay private</strong>
                <p>They’re used only to shape this set of recommendations.</p>
              </div>
            </div>
          </div>
        )}
      </div>

      <div className="planner-actions">
        <button
          className="text-button"
          onClick={() => setStep(Math.max(1, step - 1))}
          disabled={step === 1}
        >
          <Icon name="arrow_back" /> Back
        </button>
        {step < 3 ? (
          <button className="primary-button" onClick={() => setStep(step + 1)}>
            Continue <Icon name="arrow_forward" />
          </button>
        ) : (
          <button className="primary-button" onClick={findPlaces}>
            Find my places <Icon name="auto_awesome" filled />
          </button>
        )}
      </div>
      {loading && (
        <div className="loading-overlay" role="status" aria-live="polite">
          <div className="loading-shell">
            <div className="ai-orbit">
              <span><Icon name="auto_awesome" filled /></span>
              <i />
            </div>
            <h3>Finding hidden gems across India...</h3>
            <p>Matching your pace, passions and budget with places worth remembering.</p>
            <div className="skeleton-row" aria-hidden="true">
              {[1, 2, 3].map((item) => (
                <div className="skeleton-card" key={item}>
                  <div className="skeleton-image shimmer" />
                  <div className="skeleton-lines">
                    <span className="shimmer" />
                    <span className="shimmer" />
                    <span className="shimmer" />
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}
    </section>
  );
}

function DestinationCard({
  destination,
  selected,
  onCompare,
}: {
  destination: Destination;
  selected: boolean;
  onCompare: () => void;
}) {
  const [saved, setSaved] = useState(false);
  const [openDay, setOpenDay] = useState(0);
  const [showCredit, setShowCredit] = useState(false);
  const [showDetails, setShowDetails] = useState(false);
  const colors = ["transport", "food", "stay", "activities"];

  return (
    <article className="destination-card">
      <div className="card-image">
        <TravelImage src={destination.image} alt={`${destination.place}, ${destination.state}`} />
        <div className="image-shade" />
        <div className="match-badge"><Icon name="auto_awesome" filled /> {destination.match}% match</div>
        <button
          className={`save-button ${saved ? "saved" : ""}`}
          onClick={() => setSaved(!saved)}
          aria-label={saved ? "Remove from wishlist" : "Add to wishlist"}
        >
          <Icon name="favorite" filled={saved} />
        </button>
        <div className="place-title">
          <p>{destination.state}</p>
          <h3>{destination.place}</h3>
        </div>
        <div className="attribution">
          <button
            aria-label="Show photo attribution"
            onClick={() => setShowCredit(!showCredit)}
          >
            <Icon name="info" />
          </button>
          {showCredit && (
            <div className="credit-popover">
              <strong>Photo by {destination.credit || "Wikimedia Contributor"}</strong>
              <span>{destination.license || "CC BY-SA 4.0"} · Wikimedia Commons</span>
              <a href={destination.sourceUrl || destination.image} target="_blank" rel="noreferrer">
                View source <Icon name="open_in_new" />
              </a>
            </div>
          )}
        </div>
      </div>
      <div className="card-content">
        <div className="card-meta">
          <div className="tag-list">
            {destination.tags.map((tag) => <span key={tag}>{tag}</span>)}
          </div>
          <span><Icon name="schedule" /> {destination.duration}</span>
        </div>
        <p className="summary">{destination.summary}</p>
        <div className="cost-header">
          <div>
            <span>Estimated total</span>
            <strong>{destination.cost}</strong>
          </div>
          <span className="budget-fit"><Icon name="check_circle" filled /> In budget</span>
        </div>
        <div className="cost-bar" aria-label="Estimated cost breakdown">
          {destination.costs.map((width, index) => (
            <span className={colors[index]} key={colors[index]} style={{ width: `${width}%` }} />
          ))}
        </div>
        <div className="cost-legend">
          <span><i className="transport" />Travel</span>
          <span><i className="food" />Food</span>
          <span><i className="stay" />Stay</span>
          <span><i className="activities" />Things to do</span>
        </div>
        <div
          className="estimate-note"
          style={{ cursor: destination.costEstimates ? "pointer" : "default" }}
          onClick={() => setShowDetails(!showDetails)}
        >
          <Icon name="info" /> Approximate estimates, not live booked prices.
          {destination.costEstimates && <u> (Click for breakdown)</u>}
        </div>

        {showDetails && destination.costEstimates && (
          <div style={{ marginTop: "10px", padding: "10px 14px", background: "#f8f7f2", borderRadius: "10px", fontSize: "12px", border: "1px solid #deddd6" }}>
            <strong style={{ display: "block", marginBottom: "4px" }}>Detailed INR Cost Estimates (Low — Typical — High):</strong>
            <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "6px" }}>
              {destination.costEstimates.transport && <div>Travel: ₹{destination.costEstimates.transport.typical.toLocaleString()}</div>}
              {destination.costEstimates.accommodation && <div>Stay: ₹{destination.costEstimates.accommodation.typical.toLocaleString()}</div>}
              {destination.costEstimates.food && <div>Food: ₹{destination.costEstimates.food.typical.toLocaleString()}</div>}
              {destination.costEstimates.activities && <div>Activities: ₹{destination.costEstimates.activities.typical.toLocaleString()}</div>}
            </div>
            {destination.travelTips && destination.travelTips.length > 0 && (
              <div style={{ marginTop: "8px", borderTop: "1px solid #e5e4de", paddingTop: "6px" }}>
                <strong>Local Tips:</strong>
                <ul style={{ margin: "4px 0 0 16px", padding: 0 }}>
                  {destination.travelTips.map((tip) => <li key={tip}>{tip}</li>)}
                </ul>
              </div>
            )}
            {destination.uncertaintyMarkers && destination.uncertaintyMarkers.length > 0 && (
              <div style={{ marginTop: "6px", fontSize: "11px", color: "#92400e" }}>
                <strong>Notes:</strong> {destination.uncertaintyMarkers.join(" · ")}
              </div>
            )}
          </div>
        )}

        <div className="itinerary">
          <h4>Your day-by-day</h4>
          {destination.itinerary.map(([day, title, detail], index) => (
            <div className={`day ${openDay === index ? "open" : ""}`} key={day}>
              <button onClick={() => setOpenDay(openDay === index ? -1 : index)}>
                <span className="day-number">{index + 1}</span>
                <span><small>{day}</small><strong>{title}</strong></span>
                <Icon name="expand_more" />
              </button>
              <div className="day-detail"><p>{detail}</p></div>
            </div>
          ))}
        </div>
        <div className="card-actions">
          <button className={`compare-button ${selected ? "selected" : ""}`} onClick={onCompare}>
            <span className="checkbox">{selected && <Icon name="check" />}</span>
            {selected ? "Added to compare" : "Add to compare"}
          </button>
          <button className="arrow-button" aria-label={`Explore ${destination.place}`}>
            <Icon name="arrow_outward" />
          </button>
        </div>
      </div>
    </article>
  );
}

function ComparisonDrawer({
  destinationsList,
  selected,
  onRemove,
}: {
  destinationsList: Destination[];
  selected: number[];
  onRemove: (id: number) => void;
}) {
  const [expanded, setExpanded] = useState(false);
  const chosen = destinationsList.filter((item) => selected.includes(item.id));

  if (!chosen.length) return null;

  return (
    <aside className={`comparison-drawer ${expanded ? "expanded" : ""}`}>
      <button className="drawer-handle" onClick={() => setExpanded(!expanded)} aria-label="Toggle comparison">
        <span />
      </button>
      <div className="drawer-inner">
        <div className="drawer-title">
          <span className="compare-icon"><Icon name="compare_arrows" /></span>
          <div>
            <strong>Compare trips</strong>
            <span>{selected.length} of 3 selected</span>
          </div>
        </div>
        <div className="chosen-places">
          {chosen.map((item) => (
            <div className="chosen" key={item.id}>
              <TravelImage src={item.image} alt={item.place} />
              <span><strong>{item.place}</strong><small>{item.cost} · {item.duration}</small></span>
              <button onClick={() => onRemove(item.id)} aria-label={`Remove ${item.place}`}><Icon name="close" /></button>
            </div>
          ))}
        </div>
        <button className="compare-now" disabled={chosen.length < 2} onClick={() => setExpanded(true)}>
          Compare now <Icon name="arrow_upward" />
        </button>
      </div>
      {expanded && chosen.length >= 2 && (
        <div className="comparison-table">
          <div><span>Destination</span>{chosen.map((item) => <strong key={item.id}>{item.place}</strong>)}</div>
          <div><span>Match</span>{chosen.map((item) => <strong key={item.id}>{item.match}%</strong>)}</div>
          <div><span>Cost</span>{chosen.map((item) => <strong key={item.id}>{item.cost}</strong>)}</div>
          <div><span>Duration</span>{chosen.map((item) => <strong key={item.id}>{item.duration}</strong>)}</div>
        </div>
      )}
    </aside>
  );
}

export default function App() {
  const [destinationsList, setDestinationsList] = useState<Destination[]>(initialDestinations);
  const [selected, setSelected] = useState<number[]>([1, 2]);
  const [sort, setSort] = useState("match");
  const [activeFilter, setActiveFilter] = useState<"all" | "under30k" | "duration" | "gems">("all");
  const [searchQuery, setSearchQuery] = useState("");
  const [isSearching, setIsSearching] = useState(false);

  const filteredAndSortedDestinations = useMemo(() => {
    let list = [...destinationsList];

    // Filter by active pill
    if (activeFilter === "under30k") {
      list = list.filter((item) => item.costNum <= 30000);
    } else if (activeFilter === "duration") {
      list = list.filter((item) => {
        const d = parseInt(item.duration, 10);
        return isNaN(d) || (d >= 3 && d <= 5);
      });
    } else if (activeFilter === "gems") {
      list = list.filter((item) => item.match >= 90);
    }

    // Filter by search query
    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase().trim();
      list = list.filter(
        (item) =>
          item.place.toLowerCase().includes(q) ||
          item.state.toLowerCase().includes(q) ||
          item.summary.toLowerCase().includes(q) ||
          item.tags.some((t) => t.toLowerCase().includes(q)),
      );
    }

    return list.sort((a, b) =>
      sort === "cost" ? a.costNum - b.costNum : b.match - a.match,
    );
  }, [sort, destinationsList, activeFilter, searchQuery]);

  const handleQuickSearch = async (term: string) => {
    if (!term.trim()) return;
    setIsSearching(true);
    try {
      const response = await fetch("/api/recommendations", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          startingCity: "Bengaluru",
          destinationPreference: term.trim(),
          budget: 45000,
          groupBudget: true,
          travelers: 2,
          tripLengthDays: 5,
          interests: ["Heritage", "Nature", "Adventure"],
          pace: "Balanced",
          transport: "Train + local",
          stayStyle: "Boutique hotels",
          companion: "Partner",
          notes: term.trim(),
        }),
      });
      if (response.ok) {
        const data = await response.json();
        if (data.destinations && data.destinations.length > 0) {
          setDestinationsList(data.destinations);
          setSearchQuery("");
          setActiveFilter("all");
        }
      }
    } catch (e) {
      console.warn("Quick search error:", e);
    } finally {
      setIsSearching(false);
    }
  };

  const toggleCompare = (id: number) => {
    setSelected((current) =>
      current.includes(id) ? current.filter((item) => item !== id) : [...current, id].slice(-3),
    );
  };

  return (
    <main>
      <nav className="navbar">
        <a className="brand" href="#top" aria-label="whereNxt home">
          <span className="brand-mark"><Icon name="near_me" filled /></span>
          <span>where<span>Nxt</span></span>
        </a>
        <div className="nav-links">
          <a href="#planner">Plan a trip</a>
          <a href="#results">Explore</a>
          <button className="icon-button" aria-label="Open wishlist"><Icon name="favorite" /></button>
          <button className="profile-button" aria-label="Open profile">AK</button>
        </div>
      </nav>

      <header className="hero" id="top">
        <div className="hero-copy">
          <span className="hero-kicker"><i />Made for curious travellers</span>
          <h1>
            Your next story<br />
            starts <em>somewhere.</em>
          </h1>
          <p>
            Tell us what moves you. Our AI blends your budget, pace and passions into
            thoughtful journeys across incredible India.
          </p>
          <div className="hero-actions">
            <a className="hero-cta" href="#planner">
              Start exploring <span><Icon name="south" /></span>
            </a>
            <div className="traveler-proof">
              <div className="avatars">
                <span>R</span><span>M</span><span>S</span>
              </div>
              <p><strong>12,000+</strong> trips inspired</p>
            </div>
          </div>
        </div>
        <div className="hero-art" aria-label="Travel highlights from India">
          <div className="hero-main-image">
            <TravelImage src={destinationsList[0]?.image || initialDestinations[0].image} alt="Destination highlight" />
            <span className="floating-card">
              <Icon name="explore" filled />
              <span><small>Trending now</small><strong>Royal Rajasthan</strong></span>
            </span>
          </div>
          <div className="hero-small-image">
            <TravelImage src={destinationsList[1]?.image || initialDestinations[1].image} alt="Destination highlight" />
          </div>
          <div className="sun-shape"><Icon name="wb_sunny" /></div>
          <span className="dotted-path" />
        </div>
      </header>

      <Planner onRecommendations={setDestinationsList} />

      <section className="results-section" id="results">
        <div className="results-heading">
          <div>
            <span className="eyebrow"><Icon name="auto_awesome" filled /> Curated for you</span>
            <h2>Places that feel like <em>you</em></h2>
            <p>Thoughtful recommendations based on your love for heritage, food and a balanced pace.</p>
          </div>
          <div className="results-count"><strong>{filteredAndSortedDestinations.length}</strong><span>great matches</span></div>
        </div>
        <div className="toolbar" style={{ flexWrap: "wrap", gap: "12px" }}>
          <div className="filter-scroll">
            <button
              className={`filter ${activeFilter === "all" ? "active" : ""}`}
              onClick={() => setActiveFilter("all")}
            >
              <Icon name="tune" /> All matches
            </button>
            <button
              className={`filter ${activeFilter === "under30k" ? "active" : ""}`}
              onClick={() => setActiveFilter("under30k")}
            >
              Under ₹30k
            </button>
            <button
              className={`filter ${activeFilter === "duration" ? "active" : ""}`}
              onClick={() => setActiveFilter("duration")}
            >
              3–5 days
            </button>
            <button
              className={`filter ${activeFilter === "gems" ? "active" : ""}`}
              onClick={() => setActiveFilter("gems")}
            >
              Hidden gems
            </button>
          </div>

          <div style={{ display: "flex", alignItems: "center", gap: "8px", flex: "1 1 220px", maxWidth: "320px" }}>
            <div style={{ display: "flex", alignItems: "center", gap: "6px", background: "white", padding: "6px 14px", borderRadius: "999px", border: "1px solid var(--line)", width: "100%" }}>
              <Icon name="search" />
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === "Enter" && filteredAndSortedDestinations.length === 0 && searchQuery.trim()) {
                    handleQuickSearch(searchQuery);
                  }
                }}
                placeholder="Search places, states, tags..."
                style={{ border: "none", outline: "none", fontSize: "11px", width: "100%", background: "transparent" }}
              />
              {searchQuery && (
                <button
                  onClick={() => setSearchQuery("")}
                  style={{ border: "none", background: "transparent", cursor: "pointer", color: "var(--muted)", padding: 0 }}
                  aria-label="Clear search"
                >
                  <Icon name="close" />
                </button>
              )}
            </div>
          </div>

          <label className="sort-control">
            <span>Sort by</span>
            <select value={sort} onChange={(event) => setSort(event.target.value)}>
              <option value="match">Best match</option>
              <option value="cost">Lowest cost</option>
            </select>
            <Icon name="expand_more" />
          </label>
        </div>

        {filteredAndSortedDestinations.length === 0 ? (
          <div style={{ textAlign: "center", padding: "48px 24px", background: "white", borderRadius: "20px", margin: "24px 0", border: "1px solid var(--line)" }}>
            <span style={{ display: "inline-block", padding: "14px", borderRadius: "50%", background: "var(--pale-teal)", color: "var(--teal)", marginBottom: "12px" }}>
              <Icon name="travel_explore" style={{ fontSize: "32px" }} />
            </span>
            <h3 style={{ margin: "6px 0 8px", fontSize: "20px" }}>No places found matching "{searchQuery}"</h3>
            <p style={{ color: "var(--muted)", fontSize: "12px", maxWidth: "420px", margin: "0 auto 20px" }}>
              Explore "{searchQuery}" with whereNxt AI to get custom itineraries, INR cost estimates, and photos!
            </p>
            <button
              className="primary-button"
              style={{ margin: "0 auto" }}
              onClick={() => handleQuickSearch(searchQuery)}
              disabled={isSearching}
            >
              {isSearching ? "Searching with AI..." : `Discover "${searchQuery}" with AI`} <Icon name="auto_awesome" filled />
            </button>
          </div>
        ) : (
          <div className="destination-grid">
            {filteredAndSortedDestinations.map((destination) => (
              <DestinationCard
                destination={destination}
                key={destination.id}
                selected={selected.includes(destination.id)}
                onCompare={() => toggleCompare(destination.id)}
              />
            ))}
          </div>
        )}
        <button className="load-more" onClick={() => document.getElementById("planner")?.scrollIntoView({ behavior: "smooth" })}>
          Plan a tailored trip <Icon name="expand_more" />
        </button>
      </section>

      <footer>
        <a className="brand footer-brand" href="#top">
          <span className="brand-mark"><Icon name="near_me" filled /></span>
          <span>where<span>Nxt</span></span>
        </a>
        <p>Go somewhere that stays with you.</p>
        <span>Made thoughtfully in India</span>
      </footer>

      <ComparisonDrawer destinationsList={destinationsList} selected={selected} onRemove={toggleCompare} />
    </main>
  );
}
