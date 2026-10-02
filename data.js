const DESTINATIONS = [
  {
    id: "paris",
    name: "Paris",
    country: "France",
    continent: "Europe",
    tagline: "The City of Light, Art, and Timeless Romance",
    rating: 4.9,
    reviewsCount: 14200,
    vibe: "Romantic",
    coordinates: [48.8566, 2.3522],
    heroImage: "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?auto=format&fit=crop&w=1600&q=80",
    bestMonths: "Apr - Jun & Sep - Nov",
    weatherAvg: "18°C / 64°F",
    language: "French",
    currency: "Euro (€)",
    description: "Paris enchants with its iconic iron tower, grand boulevards, world-class art collections, and bohemian street cafes along the historic Seine River.",
    quickTips: [
      "Buy a Paris Museum Pass to bypass general admission queues.",
      "The Metro is the fastest and cheapest way to get across town.",
      "Greetings matter! Always say 'Bonjour' when entering shops."
    ],
    itinerary1Day: [
      "9:00 AM - Morning croissant at Montmartre & visit Sacré-Cœur",
      "12:00 PM - Explore the Louvre Museum & Mona Lisa",
      "3:30 PM - Stroll Tuileries Garden and Place de la Concorde",
      "7:30 PM - Sunset Eiffel Tower light show & Seine Dinner Cruise"
    ],
    itinerary3Day: [
      "Day 1: Classic Landmarks - Eiffel Tower, Seine Cruise, Arc de Triomphe",
      "Day 2: Art & Heritage - Louvre Museum, Musée d'Orsay, Notre-Dame",
      "Day 3: Palace Day Trip - Château de Versailles & Royal Gardens"
    ],
    attractions: [
      {
        id: "eiffel-tower",
        title: "Eiffel Tower",
        category: "Iconic Landmark",
        rating: 4.9,
        reviews: 28400,
        price: "€28.30",
        duration: "2-3 Hours",
        address: "Champ de Mars, 5 Av. Anatole France, 75007 Paris",
        image: "https://images.unsplash.com/photo-1511739001486-6bfe10ce785f?auto=format&fit=crop&w=1200&q=80",
        gallery: [
          "https://images.unsplash.com/photo-1511739001486-6bfe10ce785f?auto=format&fit=crop&w=1200&q=80",
          "https://images.unsplash.com/photo-1543349689-9a4d426bee8e?auto=format&fit=crop&w=1200&q=80",
          "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?auto=format&fit=crop&w=1200&q=80"
        ],
        description: "Gustave Eiffel's masterpiece stands 330 meters high over the Champ de Mars. Marvel at 360° panoramic views of Paris from the summit.",
        insiderTip: "Visit right before dusk so you can experience Paris in golden daylight and see the 20,000 sparkling lights switch on at nightfall.",
        highlights: ["Summit Observation Deck", "Glass Floor on 1st Level", "Champagne Bar at Top", "Nightly Hourly Sparkle Show"]
      },
      {
        id: "louvre-museum",
        title: "Louvre Museum",
        category: "Art & Culture",
        rating: 4.8,
        reviews: 31200,
        price: "€22.00",
        duration: "3-5 Hours",
        address: "75001 Paris, France",
        image: "https://images.unsplash.com/photo-1499856871958-5b9627545d1a?auto=format&fit=crop&w=1200&q=80",
        gallery: [
          "https://images.unsplash.com/photo-1499856871958-5b9627545d1a?auto=format&fit=crop&w=1200&q=80",
          "https://images.unsplash.com/photo-1565099824688-e93eb20fe622?auto=format&fit=crop&w=1200&q=80"
        ],
        description: "The world's largest art museum housed in a former royal palace, showcasing over 35,000 works including Leonardo da Vinci's Mona Lisa.",
        insiderTip: "Enter through the Carrousel du Louvre underground mall entrance to avoid the glass pyramid main queue.",
        highlights: ["Mona Lisa", "Venus de Milo", "Winged Victory of Samothrace", "I.M. Pei Glass Pyramid"]
      },
      {
        id: "montmartre-sacre-coeur",
        title: "Sacré-Cœur Basilica & Montmartre",
        category: "Historic Neighborhood",
        rating: 4.8,
        reviews: 18900,
        price: "Free Access",
        duration: "2-4 Hours",
        address: "35 Rue du Chevalier de la Barre, 75018 Paris",
        image: "https://images.unsplash.com/photo-1509299349698-ab22323ae696?auto=format&fit=crop&w=1200&q=80",
        gallery: [
          "https://images.unsplash.com/photo-1509299349698-ab22323ae696?auto=format&fit=crop&w=1200&q=80"
        ],
        description: "A hilltop neighborhood famous for its artist square (Place du Tertre), cobblestone alleys, vintage cafes, and the gleaming white dome of Sacré-Cœur.",
        insiderTip: "Climb the dome of Sacré-Cœur for the highest unobstructed view in Paris after the Eiffel Tower.",
        highlights: ["Panoramic Hillside Views", "Place du Tertre Portrait Artists", "Dome Climb"]
      }
    ]
  },
  {
    id: "tokyo",
    name: "Tokyo",
    country: "Japan",
    continent: "Asia",
    tagline: "Where Ancient Shinto Traditions Meet Neon Futures",
    rating: 4.95,
    reviewsCount: 19800,
    vibe: "Futuristic",
    coordinates: [35.6762, 139.6503],
    heroImage: "https://images.unsplash.com/photo-1503899036084-c55cdd92da26?auto=format&fit=crop&w=1600&q=80",
    bestMonths: "Mar - May & Oct - Nov",
    weatherAvg: "16°C / 61°F",
    language: "Japanese",
    currency: "Japanese Yen (¥)",
    description: "Tokyo seamlessly weaves ultramodern neon skyscrapers, bullet trains, digital art museums, anime subculture hubs, and centuries-old wooden shrines.",
    quickTips: [
      "Get a Suica or Pasmo IC card for easy subway swipes.",
      "Convenience stores (7-Eleven, Lawson) have gourmet meals on a budget.",
      "Tipping is not customary in Japan."
    ],
    itinerary1Day: [
      "8:30 AM - Early morning stroll at Senso-ji Temple in Asakusa",
      "11:30 AM - Immerse in digital art at teamLab Planets",
      "3:00 PM - Walk Meiji Jingu Forest Shrine & Harajuku Takeshita Street",
      "7:00 PM - Experience Shibuya Crossing & Shibuya Sky observation deck"
    ],
    itinerary3Day: [
      "Day 1: Historic & Modern Mix - Asakusa, Skytree, Akihabara",
      "Day 2: Culture & Shopping - Meiji Shrine, Harajuku, Omotesando, Shibuya",
      "Day 3: Digital Art & Coastal Views - Odaiba, teamLab Planets, Ginza"
    ],
    attractions: [
      {
        id: "senso-ji",
        title: "Sensō-ji Temple & Asakusa",
        category: "Historic Shrine",
        rating: 4.9,
        reviews: 24100,
        price: "Free Entry",
        duration: "2-3 Hours",
        address: "2-3-1 Asakusa, Taito City, Tokyo 111-0032",
        image: "https://images.unsplash.com/photo-1545569341-9eb8b30979d9?auto=format&fit=crop&w=1200&q=80",
        gallery: [
          "https://images.unsplash.com/photo-1545569341-9eb8b30979d9?auto=format&fit=crop&w=1200&q=80"
        ],
        description: "Tokyo's oldest Buddhist temple founded in 645 AD. Walk under the iconic Kaminarimon (Thunder Gate) giant lantern into Nakamise shopping street.",
        insiderTip: "Visit around 7:00 AM before Nakamise market opens to experience pristine serenity.",
        highlights: ["Giant Red Kaminarimon Lantern", "5-Story Pagoda", "Nakamise Traditional Arcade"]
      },
      {
        id: "shibuya-crossing-sky",
        title: "Shibuya Crossing & Shibuya Sky",
        category: "Modern Cityscape",
        rating: 4.9,
        reviews: 32000,
        price: "¥2,200 (Shibuya Sky)",
        duration: "2 Hours",
        address: "2-24-12 Shibuya, Shibuya City, Tokyo 150-0002",
        image: "https://images.unsplash.com/photo-1542051841857-5f90071e7989?auto=format&fit=crop&w=1200&q=80",
        gallery: [
          "https://images.unsplash.com/photo-1542051841857-5f90071e7989?auto=format&fit=crop&w=1200&q=80"
        ],
        description: "The world's busiest pedestrian scramble crossing where up to 3,000 people cross simultaneously, topped by an open-air rooftop sky deck.",
        insiderTip: "Reserve Shibuya Sky sunset slots 4 weeks ahead online for Mount Fuji views.",
        highlights: ["Hachiko Bronze Statue", "Rooftop Open-Air Sky Deck", "360° Neon City Skyline"]
      }
    ]
  },
  {
    id: "rome",
    name: "Rome",
    country: "Italy",
    continent: "Europe",
    tagline: "The Eternal City of Gladiators, Emperors, and Gelato",
    rating: 4.85,
    reviewsCount: 16400,
    vibe: "Historic",
    coordinates: [41.9028, 12.4964],
    heroImage: "https://images.unsplash.com/photo-1552832230-c0197dd311b5?auto=format&fit=crop&w=1600&q=80",
    bestMonths: "Apr - Jun & Sep - Oct",
    weatherAvg: "21°C / 70°F",
    language: "Italian",
    currency: "Euro (€)",
    description: "Rome is an open-air museum where ancient marble ruins, baroque fountains, Renaissance churches, and trattoria aromas line cobblestone plazas.",
    quickTips: [
      "Always order espresso at the bar counter for lower prices.",
      "Carry a reusable water bottle—Rome's free street nasoni fountains offer cold water."
    ],
    itinerary1Day: [
      "8:30 AM - Early entry to the Colosseum & Roman Forum",
      "12:30 PM - Authentic Carbonara pasta in Trastevere",
      "3:00 PM - Toss a coin into Trevi Fountain & see the Pantheon",
      "6:30 PM - Sunset Aperitivo at Piazza Navona"
    ],
    itinerary3Day: [
      "Day 1: Imperial Rome - Colosseum, Palatine Hill, Roman Forum",
      "Day 2: Vatican City - St. Peter's Basilica, Sistine Chapel, Vatican Museums",
      "Day 3: Baroque Charm - Trevi Fountain, Pantheon, Spanish Steps"
    ],
    attractions: [
      {
        id: "colosseum",
        title: "The Colosseum & Roman Forum",
        category: "Ancient Architecture",
        rating: 4.9,
        reviews: 35000,
        price: "€18.00",
        duration: "3 Hours",
        address: "Piazza del Colosseo, 1, 00184 Roma RM",
        image: "https://images.unsplash.com/photo-1552832230-c0197dd311b5?auto=format&fit=crop&w=1200&q=80",
        gallery: [
          "https://images.unsplash.com/photo-1552832230-c0197dd311b5?auto=format&fit=crop&w=1200&q=80"
        ],
        description: "The world's largest amphitheater completed in 80 AD, where gladiators fought before 50,000 spectators.",
        insiderTip: "Book an Arena Floor ticket to stand right where gladiator cages were raised.",
        highlights: ["Arena Floor View", "Underground Tunnels", "Palatine Hill View"]
      },
      {
        id: "trevi-fountain",
        title: "Trevi Fountain",
        category: "Baroque Monument",
        rating: 4.8,
        reviews: 29000,
        price: "Free Entry",
        duration: "1 Hour",
        address: "Piazza di Trevi, 00187 Roma RM",
        image: "https://images.unsplash.com/photo-1531572753322-ad063cecc140?auto=format&fit=crop&w=1200&q=80",
        gallery: [
          "https://images.unsplash.com/photo-1531572753322-ad063cecc140?auto=format&fit=crop&w=1200&q=80"
        ],
        description: "Rome's grandest baroque fountain depicting Oceanus on his chariot, famous for the coin-tossing legend.",
        insiderTip: "Toss your coin with your right hand over your left shoulder!",
        highlights: ["Oceanus Sculptures", "Traditional Coin Toss", "Night Illuminations"]
      }
    ]
  },
  {
    id: "bali",
    name: "Bali",
    country: "Indonesia",
    tagline: "Island of the Gods, Emerald Terraces & Ocean Sanctuaries",
    continent: "Asia",
    rating: 4.88,
    reviewsCount: 15100,
    vibe: "Nature",
    coordinates: [-8.4095, 115.1889],
    heroImage: "https://images.unsplash.com/photo-1537996194471-e657df975ab4?auto=format&fit=crop&w=1600&q=80",
    bestMonths: "Apr - Oct",
    weatherAvg: "27°C / 81°F",
    language: "Balinese / Indonesian",
    currency: "Indonesian Rupiah (IDR)",
    description: "Tropical paradise famed for lush terraced rice paddies, volcanic craters, vibrant Hindu temple ceremonies, and world-class retreats.",
    quickTips: [
      "Rent a scooter with helmet or hire a private day driver.",
      "Respect temple etiquette: wear a traditional sarong."
    ],
    itinerary1Day: [
      "7:00 AM - Sunrise walk through Tegalalang Rice Terraces",
      "10:00 AM - Visit Sacred Monkey Forest Sanctuary in Ubud",
      "2:00 PM - Waterfall dip at Tegenungan",
      "6:00 PM - Sunset Kecak Fire Dance at Uluwatu Cliff Temple"
    ],
    itinerary3Day: [
      "Day 1: Ubud Cultural Heart - Rice Fields, Monkey Forest, Ubud Market",
      "Day 2: Temples & Lakes - Ulun Danu Beratan, Handara Gate",
      "Day 3: Cliff Coastlines - Uluwatu Temple, Kecak Dance, Jimbaran Seafood"
    ],
    attractions: [
      {
        id: "tegalalang-ubud",
        title: "Tegalalang Rice Terraces",
        category: "Nature & Culture",
        rating: 4.8,
        reviews: 19800,
        price: "IDR 50,000",
        duration: "3 Hours",
        address: "Jl. Raya Tegallalang, Gianyar, Bali 80561",
        image: "https://images.unsplash.com/photo-1518548419970-58e3b4079ab2?auto=format&fit=crop&w=1200&q=80",
        gallery: [
          "https://images.unsplash.com/photo-1518548419970-58e3b4079ab2?auto=format&fit=crop&w=1200&q=80"
        ],
        description: "Cascading green valley sculpted with ancient UNESCO Subak irrigation rice terraces.",
        insiderTip: "Arrive at 6:30 AM to catch golden sunlight filtering through morning mist.",
        highlights: ["Subak Irrigation", "Jungle Swings", "Terrace Trails"]
      }
    ]
  },
  {
    id: "new-york",
    name: "New York City",
    country: "United States",
    tagline: "The Concrete Jungle Where Dreams Are Built",
    continent: "Americas",
    rating: 4.87,
    reviewsCount: 22000,
    vibe: "Metropolis",
    coordinates: [40.7128, -74.0060],
    heroImage: "https://images.unsplash.com/photo-1496442226666-8d4d0e62e6e9?auto=format&fit=crop&w=1600&q=80",
    bestMonths: "Apr - Jun & Sep - Nov",
    weatherAvg: "17°C / 62°F",
    language: "English",
    currency: "US Dollar ($)",
    description: "The global epicenter of theatre, art, architecture, and nightlife—spanning Central Park's green oasis to the sky decks of Manhattan.",
    quickTips: [
      "Use contactless credit cards directly at MTA subway turnstiles.",
      "Walk the Brooklyn Bridge at sunrise for empty photo ops."
    ],
    itinerary1Day: [
      "8:30 AM - Walk through Central Park & Bethesda Terrace",
      "11:30 AM - Stroll The High Line elevated park",
      "3:00 PM - Summit One Vanderbilt observation deck",
      "8:00 PM - Broadway Show & midnight Times Square lights"
    ],
    itinerary3Day: [
      "Day 1: Midtown Landmarks - Empire State, Times Square, Rockefeller",
      "Day 2: Downtown & Statue - Ferry to Statue of Liberty, 9/11 Memorial",
      "Day 3: Culture & High Line - MET Museum, High Line Park, Broadway"
    ],
    attractions: [
      {
        id: "statue-of-liberty",
        title: "Statue of Liberty",
        category: "Historic Icon",
        rating: 4.8,
        reviews: 29800,
        price: "$25.00",
        duration: "3-4 Hours",
        address: "New York, NY 10004",
        image: "https://images.unsplash.com/photo-1605130284535-11dd9edc584d?auto=format&fit=crop&w=1200&q=80",
        gallery: [
          "https://images.unsplash.com/photo-1605130284535-11dd9edc584d?auto=format&fit=crop&w=1200&q=80"
        ],
        description: "France's gift of freedom standing in New York Harbor, symbol of hope and democracy.",
        insiderTip: "Take the first ferry out at 8:30 AM from Battery Park.",
        highlights: ["Pedestal Access", "Ellis Island Museum", "Harbor Cruise"]
      }
    ]
  },
  {
    id: "cairo",
    name: "Cairo & Giza",
    country: "Egypt",
    tagline: "Land of Pharaohs, Pyramids, and Ancient Mysteries",
    continent: "Africa",
    rating: 4.82,
    reviewsCount: 12400,
    vibe: "Historic",
    coordinates: [29.9792, 31.1342],
    heroImage: "https://images.unsplash.com/photo-1503177119275-0aa32b3a9368?auto=format&fit=crop&w=1600&q=80",
    bestMonths: "Oct - Apr",
    weatherAvg: "24°C / 75°F",
    language: "Arabic",
    currency: "Egyptian Pound (EGP)",
    description: "Stand face-to-face with the last surviving wonder of the ancient world, navigate medieval bazaars, and sail the Nile.",
    quickTips: [
      "Hire an official Egyptologist guide for deep historical insights.",
      "Early morning visits avoid desert heat."
    ],
    itinerary1Day: [
      "8:00 AM - Giza Plateau Pyramids & Great Sphinx exploration",
      "12:30 PM - Traditional Koshary lunch in downtown Cairo",
      "2:30 PM - Tour Grand Egyptian Museum treasures",
      "6:00 PM - Sunset Nile Felucca sailing boat ride"
    ],
    itinerary3Day: [
      "Day 1: Ancient Giza - Great Pyramid, Sphinx, Camel Safari",
      "Day 2: Museums & Citadel - Grand Egyptian Museum, Saladin Citadel",
      "Day 3: Medieval Bazaars - Khan el-Khalili, Coptic Cairo, Sunset Felucca"
    ],
    attractions: [
      {
        id: "giza-pyramids",
        title: "Great Pyramids of Giza & Sphinx",
        category: "Ancient Wonder",
        rating: 4.95,
        reviews: 31000,
        price: "EGP 540",
        duration: "3-4 Hours",
        address: "Al Giza Desert, Giza Governorate 3512201",
        image: "https://images.unsplash.com/photo-1503177119275-0aa32b3a9368?auto=format&fit=crop&w=1200&q=80",
        gallery: [
          "https://images.unsplash.com/photo-1503177119275-0aa32b3a9368?auto=format&fit=crop&w=1200&q=80"
        ],
        description: "Built over 4,500 years ago, these colossal limestone tombs continue to awe humanity.",
        insiderTip: "Walk to Panorama Point for the iconic shot where all 3 pyramids align.",
        highlights: ["Great Pyramid Chamber", "Great Sphinx", "Sunset Camel Trek"]
      }
    ]
  },
  {
    id: "jaipur",
    name: "Jaipur",
    country: "India",
    tagline: "The Pink City of Maharajas, Forts, and Palaces",
    continent: "Asia",
    rating: 4.88,
    reviewsCount: 13800,
    vibe: "Historic",
    coordinates: [26.9124, 75.7873],
    heroImage: "https://images.unsplash.com/photo-1477587458883-47145ed94245?auto=format&fit=crop&w=1600&q=80",
    bestMonths: "Oct - Mar",
    weatherAvg: "23°C / 73°F",
    language: "Hindi / Rajasthani / English",
    currency: "Indian Rupee (₹)",
    description: "Step into royal Rajasthan with terracotta-pink sandstone palaces, hilltop fortresses, and heritage hospitality.",
    quickTips: [
      "Hire an auto-rickshaw for navigating old pink city gates.",
      "Visit Amber Fort early morning for serene courtyard photography."
    ],
    itinerary1Day: [
      "8:30 AM - Explore Amber Fort & Sheesh Mahal (Mirror Palace)",
      "12:00 PM - Photo stop at Jal Mahal (Water Palace)",
      "2:00 PM - Hawa Mahal (Palace of Winds) & City Palace museum",
      "5:30 PM - Sunset view over Jaipur from Nahargarh Fort"
    ],
    itinerary3Day: [
      "Day 1: Royal Palaces - Hawa Mahal, City Palace, Jantar Mantar",
      "Day 2: Fortresses - Amber Fort, Jaigarh Fort, Nahargarh Fort Sunset",
      "Day 3: Artisans & Markets - Johari Bazaar, Albert Hall Museum"
    ],
    attractions: [
      {
        id: "hawa-mahal",
        title: "Hawa Mahal (Palace of Winds)",
        category: "Royal Architecture",
        rating: 4.85,
        reviews: 21000,
        price: "₹200",
        duration: "1.5 Hours",
        address: "Hawa Mahal Rd, Badi Choupad, Jaipur 302002",
        image: "https://images.unsplash.com/photo-1477587458883-47145ed94245?auto=format&fit=crop&w=1200&q=80",
        gallery: [
          "https://images.unsplash.com/photo-1477587458883-47145ed94245?auto=format&fit=crop&w=1200&q=80"
        ],
        description: "A 5-story pink honeycomb facade featuring 953 intricate jharokha latticework windows.",
        insiderTip: "Cross the street to Tattoo Cafe on the 3rd floor rooftop for the perfect eye-level photo.",
        highlights: ["953 Latticework Windows", "Intricate Carvings", "Rooftop Views"]
      }
    ]
  },
  {
    id: "santorini",
    name: "Santorini",
    country: "Greece",
    tagline: "Whitewashed Cycladic Magic Over Caldera Blue Waters",
    continent: "Europe",
    rating: 4.92,
    reviewsCount: 16800,
    vibe: "Romantic",
    coordinates: [36.3932, 25.4615],
    heroImage: "https://images.unsplash.com/photo-1570077188670-e3a8d69ac5ff?auto=format&fit=crop&w=1600&q=80",
    bestMonths: "May - Oct",
    weatherAvg: "22°C / 72°F",
    language: "Greek",
    currency: "Euro (€)",
    description: "Famous for its cliff-clinging whitewashed villages, blue-domed churches, and Aegean sunsets.",
    quickTips: [
      "Book Oia sunset dinner tables weeks in advance.",
      "Wear sturdy walking shoes for stone steps."
    ],
    itinerary1Day: [
      "9:00 AM - Stroll whitewashed alleys of Fira & blue domes",
      "1:00 PM - Wine tasting at Santo Wines",
      "3:30 PM - Walk down to Ammoudi Bay for grilled octopus",
      "6:30 PM - World-famous Oia Castle Sunset"
    ],
    itinerary3Day: [
      "Day 1: Oia & Blue Domes - Oia Village walk, Castle sunset",
      "Day 2: Caldera Cruise - Catamaran sailing, Volcanic Hot Springs",
      "Day 3: History & Wine - Ancient Akrotiri, Pyrgos village"
    ],
    attractions: [
      {
        id: "oia-blue-domes",
        title: "Oia Village & Blue Domes",
        category: "Clifftop Village",
        rating: 4.95,
        reviews: 23100,
        price: "Free Access",
        duration: "3-4 Hours",
        address: "Oia 847 02, Santorini, Greece",
        image: "https://images.unsplash.com/photo-1570077188670-e3a8d69ac5ff?auto=format&fit=crop&w=1200&q=80",
        gallery: [
          "https://images.unsplash.com/photo-1570077188670-e3a8d69ac5ff?auto=format&fit=crop&w=1200&q=80"
        ],
        description: "Postcard-perfect Aegean village carved into volcanic cliffs.",
        insiderTip: "Walk early at 7:30 AM for quiet photo opportunities.",
        highlights: ["Blue Domes", "Oia Castle Sunset", "Ammoudi Bay Stairs"]
      }
    ]
  },
  {
    id: "sydney",
    name: "Sydney",
    country: "Australia",
    tagline: "Harbour Sails, Golden Beaches, and Outdoor Sunshine",
    continent: "Oceania",
    rating: 4.89,
    reviewsCount: 14700,
    vibe: "Beach",
    coordinates: [-33.8688, 151.2093],
    heroImage: "https://images.unsplash.com/photo-1506973035872-a4ec16b8e8d9?auto=format&fit=crop&w=1600&q=80",
    bestMonths: "Sep - Nov & Feb - Apr",
    weatherAvg: "22°C / 72°F",
    language: "English",
    currency: "Australian Dollar (A$)",
    description: "Australia's harbour city blending famous architectural icons, surf beaches, and coastal walks.",
    quickTips: [
      "Take the public Manly Ferry for the best cheap harbour view.",
      "Pack sunscreen—the Australian sun is intense."
    ],
    itinerary1Day: [
      "9:00 AM - Sydney Opera House Guided Tour & Circular Quay",
      "12:00 PM - Stroll the historic Rocks district & Harbour Bridge",
      "3:00 PM - Catch Manly Ferry & relax at Manly Beach",
      "6:30 PM - Waterfront dining at Darling Harbour"
    ],
    itinerary3Day: [
      "Day 1: Harbour Classics - Opera House, Harbour Bridge, Botanic Gardens",
      "Day 2: Coastal & Surf - Bondi Beach, Bondi to Coogee Walk",
      "Day 3: Nature Day Trip - Blue Mountains National Park"
    ],
    attractions: [
      {
        id: "sydney-opera-house",
        title: "Sydney Opera House",
        category: "Architectural Icon",
        rating: 4.9,
        reviews: 27900,
        price: "A$43.00",
        duration: "2 Hours",
        address: "Bennelong Point, Sydney NSW 2000",
        image: "https://images.unsplash.com/photo-1506973035872-a4ec16b8e8d9?auto=format&fit=crop&w=1200&q=80",
        gallery: [
          "https://images.unsplash.com/photo-1506973035872-a4ec16b8e8d9?auto=format&fit=crop&w=1200&q=80"
        ],
        description: "Jørn Utzon's UNESCO World Heritage building with sail-like white roofs.",
        insiderTip: "Enjoy a cocktail at the outdoor Opera Bar at sunset.",
        highlights: ["Sail Roofs", "Concert Hall", "Opera Bar Sunset"]
      }
    ]
  }
];

const CONTINENTS = ["All", "Europe", "Asia", "Americas", "Africa", "Oceania"];
const VIBES = ["All", "Romantic", "Futuristic", "Historic", "Nature", "Metropolis", "Beach"];
