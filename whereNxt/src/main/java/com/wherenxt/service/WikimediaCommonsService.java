package com.wherenxt.service;

import com.wherenxt.config.WikimediaProperties;
import com.wherenxt.dto.ImageMetadataDto;
import com.wherenxt.dto.ImageSearchResponse;
import com.wherenxt.dto.wikimedia.WikimediaImageInfo;
import com.wherenxt.dto.wikimedia.WikimediaPage;
import com.wherenxt.dto.wikimedia.WikimediaResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WikimediaCommonsService {

    private static final Logger log = LoggerFactory.getLogger(WikimediaCommonsService.class);

    private final RestTemplate restTemplate;
    private final WikimediaProperties properties;

    // Curated high-quality Wikimedia Commons fallbacks for Indian destinations
    private static final Map<String, ImageMetadataDto> CURATED_FALLBACKS = new HashMap<>();

    static {
        CURATED_FALLBACKS.put("jaipur", new ImageMetadataDto(
                "https://upload.wikimedia.org/wikipedia/commons/f/f6/Hawa_Mahal_%28The_Palace_of_Winds%29_in_Jaipur%2C_20191218_1201_9174.jpg",
                "https://upload.wikimedia.org/wikipedia/commons/f/f6/Hawa_Mahal_%28The_Palace_of_Winds%29_in_Jaipur%2C_20191218_1201_9174.jpg",
                "File:Hawa Mahal (The Palace of Winds) in Jaipur",
                "Hawa Mahal (The Palace of Winds) in Jaipur, Rajasthan, India",
                "Jakub Halun",
                "CC BY-SA 4.0",
                "https://creativecommons.org/licenses/by-sa/4.0",
                "https://commons.wikimedia.org/wiki/File:Hawa_Mahal_(The_Palace_of_Winds)_in_Jaipur,_20191218_1201_9174.jpg",
                "Photo: Jakub Halun via Wikimedia Commons, CC BY-SA 4.0"
        ));

        CURATED_FALLBACKS.put("alappuzha", new ImageMetadataDto(
                "https://upload.wikimedia.org/wikipedia/commons/e/e4/Alappuzha_Boat_Beauty_W.jpg",
                "https://upload.wikimedia.org/wikipedia/commons/e/e4/Alappuzha_Boat_Beauty_W.jpg",
                "File:Alappuzha Boat Beauty",
                "Houseboat cruising through palm-fringed backwaters of Alappuzha, Kerala",
                "Sivahari",
                "CC BY-SA 4.0",
                "https://creativecommons.org/licenses/by-sa/4.0",
                "https://commons.wikimedia.org/wiki/File:Alappuzha_Boat_Beauty_W.jpg",
                "Photo: Sivahari via Wikimedia Commons, CC BY-SA 4.0"
        ));
        CURATED_FALLBACKS.put("alleppey", CURATED_FALLBACKS.get("alappuzha"));
        CURATED_FALLBACKS.put("kerala", CURATED_FALLBACKS.get("alappuzha"));

        CURATED_FALLBACKS.put("varanasi", new ImageMetadataDto(
                "https://upload.wikimedia.org/wikipedia/commons/0/04/Ahilya_Ghat_by_the_Ganges%2C_Varanasi.jpg",
                "https://upload.wikimedia.org/wikipedia/commons/0/04/Ahilya_Ghat_by_the_Ganges%2C_Varanasi.jpg",
                "File:Ahilya Ghat by the Ganges, Varanasi",
                "Sunrise at Ahilya Ghat along the sacred Ganges river in Varanasi",
                "Firoze Edassery",
                "CC BY-SA 3.0",
                "https://creativecommons.org/licenses/by-sa/3.0",
                "https://commons.wikimedia.org/wiki/File:Ahilya_Ghat_by_the_Ganges,_Varanasi.jpg",
                "Photo: Firoze Edassery via Wikimedia Commons, CC BY-SA 3.0"
        ));
        CURATED_FALLBACKS.put("banaras", CURATED_FALLBACKS.get("varanasi"));
        CURATED_FALLBACKS.put("kashi", CURATED_FALLBACKS.get("varanasi"));

        CURATED_FALLBACKS.put("udaipur", new ImageMetadataDto(
                "https://upload.wikimedia.org/wikipedia/commons/5/53/Lake_Palace%2C_Udaipur_2018.jpg",
                "https://upload.wikimedia.org/wikipedia/commons/5/53/Lake_Palace%2C_Udaipur_2018.jpg",
                "File:Lake Palace, Udaipur",
                "Taj Lake Palace shimmering on Lake Pichola in Udaipur, Rajasthan",
                "Geri",
                "CC BY-SA 4.0",
                "https://creativecommons.org/licenses/by-sa/4.0",
                "https://commons.wikimedia.org/wiki/File:Lake_Palace,_Udaipur_2018.jpg",
                "Photo: Geri via Wikimedia Commons, CC BY-SA 4.0"
        ));

        CURATED_FALLBACKS.put("munnar", new ImageMetadataDto(
                "https://upload.wikimedia.org/wikipedia/commons/d/d4/Tea_plantations_in_Munnar.jpg",
                "https://upload.wikimedia.org/wikipedia/commons/d/d4/Tea_plantations_in_Munnar.jpg",
                "File:Tea plantations in Munnar",
                "Lush undulating tea gardens in the Western Ghats of Munnar, Kerala",
                "Bimal K",
                "CC BY-SA 3.0",
                "https://creativecommons.org/licenses/by-sa/3.0",
                "https://commons.wikimedia.org/wiki/File:Tea_plantations_in_Munnar.jpg",
                "Photo: Bimal K via Wikimedia Commons, CC BY-SA 3.0"
        ));

        CURATED_FALLBACKS.put("hampi", new ImageMetadataDto(
                "https://upload.wikimedia.org/wikipedia/commons/4/4b/Stone_Chariot_Hampi.jpg",
                "https://upload.wikimedia.org/wikipedia/commons/4/4b/Stone_Chariot_Hampi.jpg",
                "File:Stone Chariot Hampi",
                "The iconic Vijayanagara Stone Chariot in Vittala Temple complex, Hampi, Karnataka",
                "Dey.sandip",
                "CC BY-SA 3.0",
                "https://creativecommons.org/licenses/by-sa/3.0",
                "https://commons.wikimedia.org/wiki/File:Stone_Chariot_Hampi.jpg",
                "Photo: Dey.sandip via Wikimedia Commons, CC BY-SA 3.0"
        ));

        CURATED_FALLBACKS.put("goa", new ImageMetadataDto(
                "https://upload.wikimedia.org/wikipedia/commons/f/f9/The_Terekhol_River_and_Querim_Beach%2C_panoramic_view%2C_Goa%2C_India.jpg",
                "https://upload.wikimedia.org/wikipedia/commons/f/f9/The_Terekhol_River_and_Querim_Beach%2C_panoramic_view%2C_Goa%2C_India.jpg",
                "File:The Terekhol River and Querim Beach Goa",
                "Panoramic coastal views and sandy beach in Goa, India",
                "Kitsuney",
                "CC BY-SA 4.0",
                "https://creativecommons.org/licenses/by-sa/4.0",
                "https://commons.wikimedia.org/wiki/File:The_Terekhol_River_and_Querim_Beach,_panoramic_view,_Goa,_India.jpg",
                "Photo: Kitsuney via Wikimedia Commons, CC BY-SA 4.0"
        ));

        CURATED_FALLBACKS.put("manali", new ImageMetadataDto(
                "https://upload.wikimedia.org/wikipedia/commons/2/21/Grass_Deodar_Monsoon_Mist_Manali_Sep20_R16_04026.jpg",
                "https://upload.wikimedia.org/wikipedia/commons/2/21/Grass_Deodar_Monsoon_Mist_Manali_Sep20_R16_04026.jpg",
                "File:Deodar and Mist in Manali",
                "Misty deodar forests and mountains in Manali, Himachal Pradesh",
                "Timothy A. Gonsalves",
                "CC BY-SA 4.0",
                "https://creativecommons.org/licenses/by-sa/4.0",
                "https://commons.wikimedia.org/wiki/File:Grass_Deodar_Monsoon_Mist_Manali_Sep20_R16_04026.jpg",
                "Photo: Timothy A. Gonsalves via Wikimedia Commons, CC BY-SA 4.0"
        ));

        CURATED_FALLBACKS.put("rishikesh", new ImageMetadataDto(
                "https://upload.wikimedia.org/wikipedia/commons/6/64/Laxman_Jhula%2C_Rishikesh.jpg",
                "https://upload.wikimedia.org/wikipedia/commons/6/64/Laxman_Jhula%2C_Rishikesh.jpg",
                "File:Laxman Jhula Rishikesh",
                "Laxman Jhula suspension bridge over holy Ganges river in Rishikesh, Uttarakhand",
                "Shubhamprasad18",
                "CC BY-SA 4.0",
                "https://creativecommons.org/licenses/by-sa/4.0",
                "https://commons.wikimedia.org/wiki/File:Laxman_Jhula,_Rishikesh.jpg",
                "Photo: Shubhamprasad18 via Wikimedia Commons, CC BY-SA 4.0"
        ));

        CURATED_FALLBACKS.put("ladakh", new ImageMetadataDto(
                "https://upload.wikimedia.org/wikipedia/commons/4/4b/Pangong_Tso_lake_Ladakh_India.jpg",
                "https://upload.wikimedia.org/wikipedia/commons/4/4b/Pangong_Tso_lake_Ladakh_India.jpg",
                "File:Pangong Tso Lake Ladakh",
                "Pristine blue waters of high-altitude Pangong Tso Lake in Ladakh",
                "Prashant Ram",
                "CC BY-SA 3.0",
                "https://creativecommons.org/licenses/by-sa/3.0",
                "https://commons.wikimedia.org/wiki/File:Pangong_Tso_lake_Ladakh_India.jpg",
                "Photo: Prashant Ram via Wikimedia Commons, CC BY-SA 3.0"
        ));
        CURATED_FALLBACKS.put("leh", CURATED_FALLBACKS.get("ladakh"));

        CURATED_FALLBACKS.put("gokarna", new ImageMetadataDto(
                "https://upload.wikimedia.org/wikipedia/commons/9/91/Om_beach_Gokarna.jpg",
                "https://upload.wikimedia.org/wikipedia/commons/9/91/Om_beach_Gokarna.jpg",
                "File:Om Beach Gokarna",
                "Natural Om-shaped beach coastline in Gokarna, Karnataka",
                "Axis of info",
                "CC BY-SA 3.0",
                "https://creativecommons.org/licenses/by-sa/3.0",
                "https://commons.wikimedia.org/wiki/File:Om_beach_Gokarna.jpg",
                "Photo: Axis of info via Wikimedia Commons, CC BY-SA 3.0"
        ));

        CURATED_FALLBACKS.put("coorg", new ImageMetadataDto(
                "https://upload.wikimedia.org/wikipedia/commons/8/87/Coffee_Plantations_in_Coorg.jpg",
                "https://upload.wikimedia.org/wikipedia/commons/8/87/Coffee_Plantations_in_Coorg.jpg",
                "File:Coffee Plantations in Coorg",
                "Lush green coffee estates in the misty hills of Coorg, Karnataka",
                "Adithya.k.b",
                "CC BY-SA 4.0",
                "https://creativecommons.org/licenses/by-sa/4.0",
                "https://commons.wikimedia.org/wiki/File:Coffee_Plantations_in_Coorg.jpg",
                "Photo: Adithya.k.b via Wikimedia Commons, CC BY-SA 4.0"
        ));

        CURATED_FALLBACKS.put("amritsar", new ImageMetadataDto(
                "https://upload.wikimedia.org/wikipedia/commons/9/94/The_Golden_Temple_of_Amritsar%2C_Punjab_%28India%29.jpg",
                "https://upload.wikimedia.org/wikipedia/commons/9/94/The_Golden_Temple_of_Amritsar%2C_Punjab_%28India%29.jpg",
                "File:Golden Temple of Amritsar",
                "Harmandir Sahib illuminated across the Amrit Sarovar in Amritsar, Punjab",
                "Ken Wieland",
                "CC BY-SA 2.0",
                "https://creativecommons.org/licenses/by-sa/2.0",
                "https://commons.wikimedia.org/wiki/File:The_Golden_Temple_of_Amritsar,_Punjab_(India).jpg",
                "Photo: Ken Wieland via Wikimedia Commons, CC BY-SA 2.0"
        ));

        CURATED_FALLBACKS.put("darjeeling", new ImageMetadataDto(
                "https://upload.wikimedia.org/wikipedia/commons/b/b3/Darjeeling_Himalayan_Railway_Steam_Train.jpg",
                "https://upload.wikimedia.org/wikipedia/commons/b/b3/Darjeeling_Himalayan_Railway_Steam_Train.jpg",
                "File:Darjeeling Himalayan Railway",
                "Historic DHR toy steam train winding through green tea plantations in Darjeeling",
                "David Castor",
                "Public domain",
                "https://creativecommons.org/publicdomain/mark/1.0/",
                "https://commons.wikimedia.org/wiki/File:Darjeeling_Himalayan_Railway_Steam_Train.jpg",
                "Photo: David Castor via Wikimedia Commons, Public domain"
        ));

        CURATED_FALLBACKS.put("meghalaya", new ImageMetadataDto(
                "https://upload.wikimedia.org/wikipedia/commons/8/84/Double_decker_living_root_bridge_cherrapunji_meghalaya_india.jpg",
                "https://upload.wikimedia.org/wikipedia/commons/8/84/Double_decker_living_root_bridge_cherrapunji_meghalaya_india.jpg",
                "File:Double Decker Living Root Bridge",
                "Ancient bio-engineered living root bridge in Cherrapunji, Meghalaya",
                "Roman Korzh",
                "CC BY-SA 3.0",
                "https://creativecommons.org/licenses/by-sa/3.0",
                "https://commons.wikimedia.org/wiki/File:Double_decker_living_root_bridge_cherrapunji_meghalaya_india.jpg",
                "Photo: Roman Korzh via Wikimedia Commons, CC BY-SA 3.0"
        ));
        CURATED_FALLBACKS.put("shillong", CURATED_FALLBACKS.get("meghalaya"));

        CURATED_FALLBACKS.put("ooty", new ImageMetadataDto(
                "https://upload.wikimedia.org/wikipedia/commons/7/7b/Ooty_tea_estate.jpg",
                "https://upload.wikimedia.org/wikipedia/commons/7/7b/Ooty_tea_estate.jpg",
                "File:Ooty Tea Estate",
                "Rolling emerald tea estates in the Nilgiri hills of Ooty, Tamil Nadu",
                "Kannan Shanmugam",
                "CC BY-SA 4.0",
                "https://creativecommons.org/licenses/by-sa/4.0",
                "https://commons.wikimedia.org/wiki/File:Ooty_tea_estate.jpg",
                "Photo: Kannan Shanmugam via Wikimedia Commons, CC BY-SA 4.0"
        ));

        CURATED_FALLBACKS.put("pondicherry", new ImageMetadataDto(
                "https://upload.wikimedia.org/wikipedia/commons/f/f3/Promenade_beach_puducherry.jpg",
                "https://upload.wikimedia.org/wikipedia/commons/f/f3/Promenade_beach_puducherry.jpg",
                "File:Promenade Beach Puducherry",
                "Promenade beach and coastal heritage in Puducherry, India",
                "Deependra",
                "CC BY-SA 3.0",
                "https://creativecommons.org/licenses/by-sa/3.0",
                "https://commons.wikimedia.org/wiki/File:Promenade_beach_puducherry.jpg",
                "Photo: Deependra via Wikimedia Commons, CC BY-SA 3.0"
        ));
        CURATED_FALLBACKS.put("puducherry", CURATED_FALLBACKS.get("pondicherry"));

        CURATED_FALLBACKS.put("kashmir", new ImageMetadataDto(
                "https://upload.wikimedia.org/wikipedia/commons/1/17/Dal_Lake_Srinagar_Kashmir.jpg",
                "https://upload.wikimedia.org/wikipedia/commons/1/17/Dal_Lake_Srinagar_Kashmir.jpg",
                "File:Dal Lake Srinagar Kashmir",
                "Traditional wooden Shikara boat on picturesque Dal Lake in Srinagar, Kashmir",
                "Basharat Alam Shah",
                "CC BY 2.0",
                "https://creativecommons.org/licenses/by/2.0",
                "https://commons.wikimedia.org/wiki/File:Dal_Lake_Srinagar_Kashmir.jpg",
                "Photo: Basharat Alam Shah via Wikimedia Commons, CC BY 2.0"
        ));
        CURATED_FALLBACKS.put("srinagar", CURATED_FALLBACKS.get("kashmir"));

        CURATED_FALLBACKS.put("shimla", new ImageMetadataDto(
                "https://upload.wikimedia.org/wikipedia/commons/7/77/Christ_Church_Shimla_night.jpg",
                "https://upload.wikimedia.org/wikipedia/commons/7/77/Christ_Church_Shimla_night.jpg",
                "File:Christ Church Shimla",
                "Historic Christ Church on the Ridge illuminated in Shimla, Himachal Pradesh",
                "Nipun Sohanlal",
                "CC BY-SA 3.0",
                "https://creativecommons.org/licenses/by-sa/3.0",
                "https://commons.wikimedia.org/wiki/File:Christ_Church_Shimla_night.jpg",
                "Photo: Nipun Sohanlal via Wikimedia Commons, CC BY-SA 3.0"
        ));

        CURATED_FALLBACKS.put("jaisalmer", new ImageMetadataDto(
                "https://upload.wikimedia.org/wikipedia/commons/e/ec/Jaisalmer_Fort_2019.jpg",
                "https://upload.wikimedia.org/wikipedia/commons/e/ec/Jaisalmer_Fort_2019.jpg",
                "File:Jaisalmer Fort",
                "The Golden Fort rising above the golden sands of the Thar Desert in Jaisalmer, Rajasthan",
                "Firoze Edassery",
                "CC BY-SA 3.0",
                "https://creativecommons.org/licenses/by-sa/3.0",
                "https://commons.wikimedia.org/wiki/File:Jaisalmer_Fort_2019.jpg",
                "Photo: Firoze Edassery via Wikimedia Commons, CC BY-SA 3.0"
        ));

        CURATED_FALLBACKS.put("agra", new ImageMetadataDto(
                "https://upload.wikimedia.org/wikipedia/commons/1/1d/Taj_Mahal_%28Edited%29.jpeg",
                "https://upload.wikimedia.org/wikipedia/commons/1/1d/Taj_Mahal_%28Edited%29.jpeg",
                "File:Taj Mahal (Edited)",
                "The iconic ivory-white marble Taj Mahal in Agra, Uttar Pradesh",
                "Muhammad Mahdi Karim",
                "GNU FDL 1.2 / CC BY-SA 3.0",
                "https://creativecommons.org/licenses/by-sa/3.0",
                "https://commons.wikimedia.org/wiki/File:Taj_Mahal_(Edited).jpeg",
                "Photo: Muhammad Mahdi Karim via Wikimedia Commons, CC BY-SA 3.0"
        ));

        CURATED_FALLBACKS.put("jabalpur", new ImageMetadataDto(
                "https://upload.wikimedia.org/wikipedia/commons/a/a8/Dhuandhar_Falls_in_Bhedaghat.JPG",
                "https://upload.wikimedia.org/wikipedia/commons/a/a8/Dhuandhar_Falls_in_Bhedaghat.JPG",
                "File:Dhuandhar Falls in Bhedaghat",
                "Majestic Dhuandhar waterfall roaring through marble rocks in Jabalpur, Madhya Pradesh",
                "Sandyadav080",
                "CC BY-SA 3.0",
                "https://creativecommons.org/licenses/by-sa/3.0",
                "https://commons.wikimedia.org/wiki/File:Dhuandhar_Falls_in_Bhedaghat.JPG",
                "Photo: Sandyadav080 via Wikimedia Commons, CC BY-SA 3.0"
        ));
        CURATED_FALLBACKS.put("bhedaghat", CURATED_FALLBACKS.get("jabalpur"));

        CURATED_FALLBACKS.put("pachmarhi", new ImageMetadataDto(
                "https://upload.wikimedia.org/wikipedia/commons/b/bd/Bee_Falls%2C_Pachmarhi_01.jpg",
                "https://upload.wikimedia.org/wikipedia/commons/b/bd/Bee_Falls%2C_Pachmarhi_01.jpg",
                "File:Bee Falls, Pachmarhi",
                "Cascading waters of Bee Falls in the hill station of Pachmarhi, Madhya Pradesh",
                "Manish.aswani",
                "CC BY-SA 4.0",
                "https://creativecommons.org/licenses/by-sa/4.0",
                "https://commons.wikimedia.org/wiki/File:Bee_Falls%2C_Pachmarhi_01.jpg",
                "Photo: Manish.aswani via Wikimedia Commons, CC BY-SA 4.0"
        ));

        // Regional aliases
        CURATED_FALLBACKS.put("rajasthan", CURATED_FALLBACKS.get("jaipur"));
        CURATED_FALLBACKS.put("kerala", CURATED_FALLBACKS.get("alappuzha"));
        CURATED_FALLBACKS.put("himachal", CURATED_FALLBACKS.get("manali"));
        CURATED_FALLBACKS.put("uttarakhand", CURATED_FALLBACKS.get("rishikesh"));
        CURATED_FALLBACKS.put("madhya pradesh", CURATED_FALLBACKS.get("jabalpur"));
        CURATED_FALLBACKS.put("mp", CURATED_FALLBACKS.get("jabalpur"));

        CURATED_FALLBACKS.put("default", CURATED_FALLBACKS.get("agra"));
    }

    public WikimediaCommonsService(RestTemplate restTemplate, WikimediaProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    /**
     * Search for images on Wikimedia Commons using the MediaWiki generator=search API.
     */
    public ImageSearchResponse searchImages(String query, int limit) {
        if (query == null || query.trim().isEmpty()) {
            return new ImageSearchResponse(query, 0, Collections.emptyList());
        }

        int queryLimit = Math.max(1, Math.min(limit, 10));
        int thumbWidth = properties.getDefaultThumbnailWidth();

        try {
            // Build URI for MediaWiki API
            URI uri = UriComponentsBuilder.fromHttpUrl(properties.getBaseUrl())
                    .queryParam("action", "query")
                    .queryParam("generator", "search")
                    .queryParam("gsrsearch", query)
                    .queryParam("gsrnamespace", "6") // Namespace 6 = File:
                    .queryParam("gsrlimit", String.valueOf(queryLimit))
                    .queryParam("prop", "imageinfo")
                    .queryParam("iiprop", "url|extmetadata|dimensions|mime")
                    .queryParam("iiurlwidth", String.valueOf(thumbWidth))
                    .queryParam("format", "json")
                    .build()
                    .toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.USER_AGENT, properties.getUserAgent());
            headers.set(HttpHeaders.ACCEPT, "application/json");

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<WikimediaResponse> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    entity,
                    WikimediaResponse.class
            );

            List<ImageMetadataDto> results = parseWikimediaResponse(response.getBody(), query);
            if (!results.isEmpty()) {
                return new ImageSearchResponse(query, results.size(), results);
            }

            log.info("No Wikimedia Commons results for query '{}', checking fallback", query);
            ImageMetadataDto fallback = getCuratedFallback(query);
            return new ImageSearchResponse(query, 1, List.of(fallback));

        } catch (Exception ex) {
            log.warn("Wikimedia Commons search failed for '{}': {}. Using fallback.", query, ex.getMessage());
            ImageMetadataDto fallback = getCuratedFallback(query);
            return new ImageSearchResponse(query, 1, List.of(fallback));
        }
    }

    /**
     * Get the single best image for a destination, with graceful fallback.
     */
    public ImageMetadataDto getBestDestinationImage(String place, String state) {
        // First check if we have a curated high-res verified image for this destination
        ImageMetadataDto curated = getCuratedFallback(place);
        if (curated != null && !curated.equals(CURATED_FALLBACKS.get("default"))) {
            return curated;
        }

        String searchQuery = place + " " + (state != null ? state : "") + " landmark filetype:bitmap";
        ImageSearchResponse searchResponse = searchImages(searchQuery.trim(), 3);
        if (searchResponse.getImages() != null && !searchResponse.getImages().isEmpty()) {
            return searchResponse.getImages().get(0);
        }

        // Secondary search with just place name
        ImageSearchResponse secondaryResponse = searchImages(place + " filetype:bitmap", 1);
        if (secondaryResponse.getImages() != null && !secondaryResponse.getImages().isEmpty()) {
            return secondaryResponse.getImages().get(0);
        }

        return getCuratedFallback(place);
    }

    private List<ImageMetadataDto> parseWikimediaResponse(WikimediaResponse body, String query) {
        if (body == null || body.getQuery() == null || body.getQuery().getPages() == null) {
            return Collections.emptyList();
        }

        List<ImageMetadataDto> imageList = new ArrayList<>();
        for (WikimediaPage page : body.getQuery().getPages().values()) {
            if (page.getImageinfo() == null || page.getImageinfo().isEmpty()) {
                continue;
            }

            WikimediaImageInfo info = page.getImageinfo().get(0);
            if (info.getMime() != null && !info.getMime().startsWith("image/")) {
                continue;
            }
            String rawOriginalUrl = info.getUrl();
            String rawThumbUrl = info.getThumburl() != null ? info.getThumburl() : rawOriginalUrl;
            String originalUrl = cleanWikimediaImageUrl(rawOriginalUrl);
            String thumbUrl = cleanWikimediaImageUrl(rawThumbUrl);
            String sourcePageUrl = info.getDescriptionurl();

            // Extract metadata with HTML cleaning
            String rawArtist = info.getMetadataValue("Artist");
            String artist = cleanHtml(rawArtist);
            if (artist.isEmpty()) {
                artist = cleanHtml(info.getMetadataValue("Credit"));
            }
            if (artist.isEmpty()) {
                artist = "Wikimedia Commons Contributor";
            }

            String license = info.getMetadataValue("LicenseShortName");
            if (license == null || license.isEmpty()) {
                license = info.getMetadataValue("UsageTerms");
            }
            if (license == null || license.isEmpty()) {
                license = "Creative Commons / Public Domain";
            }

            String licenseUrl = info.getMetadataValue("LicenseUrl");
            String rawDesc = info.getMetadataValue("ImageDescription");
            String description = cleanHtml(rawDesc);
            if (description.isEmpty()) {
                description = page.getTitle() != null ? page.getTitle().replace("File:", "") : query;
            }

            String attributionText = String.format("Photo: %s via Wikimedia Commons, licensed under %s", artist, license);

            ImageMetadataDto dto = new ImageMetadataDto(
                    originalUrl,
                    thumbUrl,
                    page.getTitle(),
                    description,
                    artist,
                    license,
                    licenseUrl,
                    sourcePageUrl,
                    attributionText
            );
            imageList.add(dto);
        }

        return imageList;
    }

    public ImageMetadataDto getCuratedFallback(String destination) {
        if (destination != null) {
            String lower = destination.toLowerCase().trim();
            for (Map.Entry<String, ImageMetadataDto> entry : CURATED_FALLBACKS.entrySet()) {
                if (lower.contains(entry.getKey())) {
                    return entry.getValue();
                }
            }
        }
        return CURATED_FALLBACKS.get("default");
    }

    public static String cleanHtml(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]*>", "")
                .replaceAll("&nbsp;", " ")
                .replaceAll("&amp;", "&")
                .replaceAll("&quot;", "\"")
                .replaceAll("&#39;", "'")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * Cleans Wikimedia URLs by converting dynamic thumbnail URLs to standard upload.wikimedia.org
     * and stripping tracking query parameters (?utm_source=...).
     */
    public static String cleanWikimediaImageUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return url;
        }
        String cleaned = url.trim();
        int qIdx = cleaned.indexOf('?');
        if (qIdx != -1) {
            cleaned = cleaned.substring(0, qIdx);
        }
        if (cleaned.contains("/commons/thumb/")) {
            cleaned = cleaned.replace("/commons/thumb/", "/commons/");
            int lastSlash = cleaned.lastIndexOf('/');
            if (lastSlash != -1 && cleaned.substring(lastSlash).matches("/\\d+px-.*")) {
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

    /**
     * Server-side proxy for Wikimedia Commons images to bypass client-side CORS, adblockers, or network blocks.
     */
    public ResponseEntity<byte[]> proxyImage(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String targetUrl = cleanWikimediaImageUrl(imageUrl);
        if (!targetUrl.contains("wikimedia.org") && !targetUrl.contains("wikipedia.org")) {
            return ResponseEntity.badRequest().build();
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.USER_AGENT, properties.getUserAgent());
            headers.set(HttpHeaders.ACCEPT, "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8");

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            URI uri = UriComponentsBuilder.fromHttpUrl(targetUrl).build().toUri();
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    entity,
                    byte[].class
            );

            HttpHeaders responseHeaders = new HttpHeaders();
            if (response.getHeaders().getContentType() != null) {
                responseHeaders.setContentType(response.getHeaders().getContentType());
            } else {
                responseHeaders.setContentType(MediaType.IMAGE_JPEG);
            }
            responseHeaders.setCacheControl(CacheControl.maxAge(Duration.ofDays(7)).cachePublic());
            responseHeaders.set("Access-Control-Allow-Origin", "*");

            return new ResponseEntity<>(response.getBody(), responseHeaders, HttpStatus.OK);
        } catch (Exception ex) {
            log.warn("Failed to proxy image {}: {}", targetUrl, ex.getMessage());
            // Attempt to serve default fallback image if proxy fails
            try {
                String fallbackUrl = CURATED_FALLBACKS.get("default").getOriginalUrl();
                HttpHeaders headers = new HttpHeaders();
                headers.set(HttpHeaders.USER_AGENT, properties.getUserAgent());
                HttpEntity<Void> entity = new HttpEntity<>(headers);
                ResponseEntity<byte[]> fallbackResp = restTemplate.exchange(
                        URI.create(fallbackUrl),
                        HttpMethod.GET,
                        entity,
                        byte[].class
                );
                return new ResponseEntity<>(fallbackResp.getBody(), HttpStatus.OK);
            } catch (Exception ignored) {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
            }
        }
    }
}
