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

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class WikimediaCommonsService {

    private static final Logger log = LoggerFactory.getLogger(WikimediaCommonsService.class);

    private final RestTemplate restTemplate;
    private final WikimediaProperties properties;

    // Curated high-quality, verified Wikimedia Commons destination photographs
    private static final Map<String, ImageMetadataDto> CURATED_FALLBACKS = new HashMap<>();

    // Destination-specific genuine landmark mappings for dynamic, relevance-aware searches
    private static final Map<String, List<String>> DESTINATION_LANDMARKS = new HashMap<>();

    static {
        // --- 1. Destination-Specific Genuine Landmarks ---
        DESTINATION_LANDMARKS.put("srinagar", List.of("Dal Lake", "Mughal gardens", "Shalimar Bagh", "Nishat Bagh", "Shankaracharya Temple"));
        DESTINATION_LANDMARKS.put("kashmir", List.of("Dal Lake", "Gulmarg", "Pahalgam", "Betaab Valley"));
        DESTINATION_LANDMARKS.put("jaipur", List.of("Hawa Mahal", "Amber Fort", "Jal Mahal", "City Palace Jaipur"));
        DESTINATION_LANDMARKS.put("udaipur", List.of("Lake Pichola", "City Palace Udaipur", "Jag Mandir", "Fateh Sagar Lake"));
        DESTINATION_LANDMARKS.put("jodhpur", List.of("Mehrangarh Fort", "Jaswant Thada", "Umaid Bhawan"));
        DESTINATION_LANDMARKS.put("jaisalmer", List.of("Jaisalmer Fort", "Sam Sand Dunes", "Patwon Ki Haveli", "Gadisar Lake"));
        DESTINATION_LANDMARKS.put("agra", List.of("Taj Mahal", "Agra Fort", "Mehtab Bagh"));
        DESTINATION_LANDMARKS.put("manali", List.of("Solang Valley", "Rohtang Pass", "Hadimba Temple", "Jogini Waterfall"));
        DESTINATION_LANDMARKS.put("shimla", List.of("Christ Church Shimla", "The Ridge Shimla", "Jakhoo Temple", "Mall Road"));
        DESTINATION_LANDMARKS.put("rishikesh", List.of("Laxman Jhula", "Triveni Ghat", "Ram Jhula", "Beatles Ashram"));
        DESTINATION_LANDMARKS.put("varanasi", List.of("Dashashwamedh Ghat", "Assi Ghat", "Ganga Aarti Varanasi", "Kashi Vishwanath"));
        DESTINATION_LANDMARKS.put("amritsar", List.of("Golden Temple Amritsar", "Harmandir Sahib", "Wagah Border"));
        DESTINATION_LANDMARKS.put("goa", List.of("Palolem Beach", "Basilica of Bom Jesus", "Fort Aguada", "Calangute Beach"));
        DESTINATION_LANDMARKS.put("alappuzha", List.of("Alappuzha Houseboat", "Vembanad Lake", "Alleppey Backwaters"));
        DESTINATION_LANDMARKS.put("alleppey", List.of("Alappuzha Houseboat", "Vembanad Lake", "Alleppey Backwaters"));
        DESTINATION_LANDMARKS.put("munnar", List.of("Tea plantations Munnar", "Mattupetty Dam", "Anamudi Peak", "Eravikulam"));
        DESTINATION_LANDMARKS.put("hampi", List.of("Stone Chariot Hampi", "Virupaksha Temple", "Vijayanagara ruins"));
        DESTINATION_LANDMARKS.put("gokarna", List.of("Om Beach Gokarna", "Kudle Beach", "Mahabaleshwar Temple Gokarna"));
        DESTINATION_LANDMARKS.put("coorg", List.of("Abbey Falls", "Raja Seat Madikeri", "Coffee plantations Coorg"));
        DESTINATION_LANDMARKS.put("darjeeling", List.of("Darjeeling Himalayan Railway", "Tiger Hill", "Batasia Loop"));
        DESTINATION_LANDMARKS.put("meghalaya", List.of("Double Decker Living Root Bridge", "Nohkalikai Falls", "Dawki River"));
        DESTINATION_LANDMARKS.put("shillong", List.of("Umiam Lake", "Elephant Falls Shillong", "Ward Lake"));
        DESTINATION_LANDMARKS.put("cherrapunji", List.of("Living Root Bridge", "Nohkalikai Falls", "Mawsmai Cave"));
        DESTINATION_LANDMARKS.put("ooty", List.of("Nilgiri Mountain Railway", "Ooty Lake", "Doddabetta Peak", "Botanical Gardens Ooty"));
        DESTINATION_LANDMARKS.put("pondicherry", List.of("Promenade Beach Puducherry", "French Quarter Pondicherry", "Auroville"));
        DESTINATION_LANDMARKS.put("puducherry", List.of("Promenade Beach Puducherry", "French Quarter Pondicherry", "Auroville"));
        DESTINATION_LANDMARKS.put("ladakh", List.of("Pangong Tso", "Khardung La", "Nubra Valley", "Thiksey Monastery"));
        DESTINATION_LANDMARKS.put("leh", List.of("Pangong Tso", "Leh Palace", "Shanti Stupa Leh"));
        DESTINATION_LANDMARKS.put("jabalpur", List.of("Dhuandhar Falls", "Marble Rocks Bhedaghat", "Chausath Yogini"));
        DESTINATION_LANDMARKS.put("bhedaghat", List.of("Dhuandhar Falls", "Marble Rocks Bhedaghat"));
        DESTINATION_LANDMARKS.put("pachmarhi", List.of("Bee Falls Pachmarhi", "Dhoopgarh", "Jata Shankar Caves"));
        DESTINATION_LANDMARKS.put("delhi", List.of("India Gate", "Red Fort Delhi", "Humayun Tomb", "Qutub Minar"));
        DESTINATION_LANDMARKS.put("new delhi", List.of("India Gate", "Red Fort Delhi", "Humayun Tomb", "Qutub Minar"));
        DESTINATION_LANDMARKS.put("mumbai", List.of("Gateway of India", "Marine Drive Mumbai", "Chhatrapati Shivaji Maharaj Terminus"));
        DESTINATION_LANDMARKS.put("kolkata", List.of("Victoria Memorial Kolkata", "Howrah Bridge", "Dakshineswar Temple"));
        DESTINATION_LANDMARKS.put("chennai", List.of("Marina Beach Chennai", "Kapaleeshwarar Temple", "San Thome Basilica"));
        DESTINATION_LANDMARKS.put("hyderabad", List.of("Charminar", "Golconda Fort", "Hussain Sagar Lake"));
        DESTINATION_LANDMARKS.put("bengaluru", List.of("Bangalore Palace", "Lalbagh Botanical Garden", "Cubbon Park"));
        DESTINATION_LANDMARKS.put("bangalore", List.of("Bangalore Palace", "Lalbagh Botanical Garden", "Cubbon Park"));
        DESTINATION_LANDMARKS.put("kochi", List.of("Chinese Fishing Nets Kochi", "Fort Kochi", "Mattancherry Palace"));
        DESTINATION_LANDMARKS.put("mysore", List.of("Mysore Palace", "Chamundi Hill", "Brindavan Gardens"));
        DESTINATION_LANDMARKS.put("khajuraho", List.of("Kandariya Mahadeva Temple", "Khajuraho Temples"));
        DESTINATION_LANDMARKS.put("chettinad", List.of("Chettinad Palace", "Chettinad Mansions", "Kanadukathan", "Athangudi"));

        // --- 2. Curated Verified Destination Images ---
        CURATED_FALLBACKS.put("jaipur", new ImageMetadataDto(
                "https://upload.wikimedia.org/wikipedia/commons/f/f6/Hawa_Mahal_%28The_Palace_of_Winds%29_in_Jaipur%2C_20191218_1201_9174.jpg",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f6/Hawa_Mahal_%28The_Palace_of_Winds%29_in_Jaipur%2C_20191218_1201_9174.jpg/800px-Hawa_Mahal_%28The_Palace_of_Winds%29_in_Jaipur%2C_20191218_1201_9174.jpg",
                "File:Hawa Mahal (The Palace of Winds) in Jaipur",
                "Hawa Mahal (The Palace of Winds) in Jaipur, Rajasthan, India",
                "Jakub Halun",
                "CC BY-SA 4.0",
                "https://creativecommons.org/licenses/by-sa/4.0",
                "https://commons.wikimedia.org/wiki/File:Hawa_Mahal_(The_Palace_of_Winds)_in_Jaipur,_20191218_1201_9174.jpg",
                "Photo: Jakub Halun via Wikimedia Commons, CC BY-SA 4.0"
        ));

        CURATED_FALLBACKS.put("udaipur", new ImageMetadataDto(
                "https://upload.wikimedia.org/wikipedia/commons/5/53/Lake_Palace%2C_Udaipur_2018.jpg",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/5/53/Lake_Palace%2C_Udaipur_2018.jpg/800px-Lake_Palace%2C_Udaipur_2018.jpg",
                "File:Lake Palace, Udaipur",
                "Taj Lake Palace shimmering on Lake Pichola in Udaipur, Rajasthan",
                "Geri",
                "CC BY-SA 4.0",
                "https://creativecommons.org/licenses/by-sa/4.0",
                "https://commons.wikimedia.org/wiki/File:Lake_Palace,_Udaipur_2018.jpg",
                "Photo: Geri via Wikimedia Commons, CC BY-SA 4.0"
        ));

        CURATED_FALLBACKS.put("srinagar", new ImageMetadataDto(
                "https://upload.wikimedia.org/wikipedia/commons/1/17/Dal_Lake_Srinagar_Kashmir.jpg",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/1/17/Dal_Lake_Srinagar_Kashmir.jpg/800px-Dal_Lake_Srinagar_Kashmir.jpg",
                "File:Dal Lake Srinagar Kashmir",
                "Traditional wooden Shikara boat on picturesque Dal Lake in Srinagar, Kashmir",
                "Basharat Alam Shah",
                "CC BY 2.0",
                "https://creativecommons.org/licenses/by/2.0",
                "https://commons.wikimedia.org/wiki/File:Dal_Lake_Srinagar_Kashmir.jpg",
                "Photo: Basharat Alam Shah via Wikimedia Commons, CC BY 2.0"
        ));
        CURATED_FALLBACKS.put("kashmir", CURATED_FALLBACKS.get("srinagar"));

        CURATED_FALLBACKS.put("alappuzha", new ImageMetadataDto(
                "https://upload.wikimedia.org/wikipedia/commons/e/e4/Alappuzha_Boat_Beauty_W.jpg",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e4/Alappuzha_Boat_Beauty_W.jpg/800px-Alappuzha_Boat_Beauty_W.jpg",
                "File:Alappuzha Boat Beauty",
                "Houseboat cruising through palm-fringed backwaters of Alappuzha, Kerala",
                "Sivahari",
                "CC BY-SA 4.0",
                "https://creativecommons.org/licenses/by-sa/4.0",
                "https://commons.wikimedia.org/wiki/File:Alappuzha_Boat_Beauty_W.jpg",
                "Photo: Sivahari via Wikimedia Commons, CC BY-SA 4.0"
        ));
        CURATED_FALLBACKS.put("alleppey", CURATED_FALLBACKS.get("alappuzha"));

        CURATED_FALLBACKS.put("varanasi", new ImageMetadataDto(
                "https://upload.wikimedia.org/wikipedia/commons/0/04/Ahilya_Ghat_by_the_Ganges%2C_Varanasi.jpg",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/0/04/Ahilya_Ghat_by_the_Ganges%2C_Varanasi.jpg/800px-Ahilya_Ghat_by_the_Ganges%2C_Varanasi.jpg",
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

        CURATED_FALLBACKS.put("munnar", new ImageMetadataDto(
                "https://upload.wikimedia.org/wikipedia/commons/d/d4/Tea_plantations_in_Munnar.jpg",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d4/Tea_plantations_in_Munnar.jpg/800px-Tea_plantations_in_Munnar.jpg",
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
                "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4b/Stone_Chariot_Hampi.jpg/800px-Stone_Chariot_Hampi.jpg",
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
                "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f9/The_Terekhol_River_and_Querim_Beach%2C_panoramic_view%2C_Goa%2C_India.jpg/800px-The_Terekhol_River_and_Querim_Beach%2C_panoramic_view%2C_Goa%2C_India.jpg",
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
                "https://upload.wikimedia.org/wikipedia/commons/thumb/2/21/Grass_Deodar_Monsoon_Mist_Manali_Sep20_R16_04026.jpg/800px-Grass_Deodar_Monsoon_Mist_Manali_Sep20_R16_04026.jpg",
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
                "https://upload.wikimedia.org/wikipedia/commons/thumb/6/64/Laxman_Jhula%2C_Rishikesh.jpg/800px-Laxman_Jhula%2C_Rishikesh.jpg",
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
                "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4b/Pangong_Tso_lake_Ladakh_India.jpg/800px-Pangong_Tso_lake_Ladakh_India.jpg",
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
                "https://upload.wikimedia.org/wikipedia/commons/thumb/9/91/Om_beach_Gokarna.jpg/800px-Om_beach_Gokarna.jpg",
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
                "https://upload.wikimedia.org/wikipedia/commons/thumb/8/87/Coffee_Plantations_in_Coorg.jpg/800px-Coffee_Plantations_in_Coorg.jpg",
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
                "https://upload.wikimedia.org/wikipedia/commons/thumb/9/94/The_Golden_Temple_of_Amritsar%2C_Punjab_%28India%29.jpg/800px-The_Golden_Temple_of_Amritsar%2C_Punjab_%28India%29.jpg",
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
                "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b3/Darjeeling_Himalayan_Railway_Steam_Train.jpg/800px-Darjeeling_Himalayan_Railway_Steam_Train.jpg",
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
                "https://upload.wikimedia.org/wikipedia/commons/thumb/8/84/Double_decker_living_root_bridge_cherrapunji_meghalaya_india.jpg/800px-Double_decker_living_root_bridge_cherrapunji_meghalaya_india.jpg",
                "File:Double Decker Living Root Bridge",
                "Ancient bio-engineered living root bridge in Cherrapunji, Meghalaya",
                "Roman Korzh",
                "CC BY-SA 3.0",
                "https://creativecommons.org/licenses/by-sa/3.0",
                "https://commons.wikimedia.org/wiki/File:Double_decker_living_root_bridge_cherrapunji_meghalaya_india.jpg",
                "Photo: Roman Korzh via Wikimedia Commons, CC BY-SA 3.0"
        ));
        CURATED_FALLBACKS.put("shillong", CURATED_FALLBACKS.get("meghalaya"));
        CURATED_FALLBACKS.put("cherrapunji", CURATED_FALLBACKS.get("meghalaya"));

        CURATED_FALLBACKS.put("ooty", new ImageMetadataDto(
                "https://upload.wikimedia.org/wikipedia/commons/7/7b/Ooty_tea_estate.jpg",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/7/7b/Ooty_tea_estate.jpg/800px-Ooty_tea_estate.jpg",
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
                "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f3/Promenade_beach_puducherry.jpg/800px-Promenade_beach_puducherry.jpg",
                "File:Promenade Beach Puducherry",
                "Promenade beach and coastal heritage in Puducherry, India",
                "Deependra",
                "CC BY-SA 3.0",
                "https://creativecommons.org/licenses/by-sa/3.0",
                "https://commons.wikimedia.org/wiki/File:Promenade_beach_puducherry.jpg",
                "Photo: Deependra via Wikimedia Commons, CC BY-SA 3.0"
        ));
        CURATED_FALLBACKS.put("puducherry", CURATED_FALLBACKS.get("pondicherry"));

        CURATED_FALLBACKS.put("shimla", new ImageMetadataDto(
                "https://upload.wikimedia.org/wikipedia/commons/7/77/Christ_Church_Shimla_night.jpg",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/7/77/Christ_Church_Shimla_night.jpg/800px-Christ_Church_Shimla_night.jpg",
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
                "https://upload.wikimedia.org/wikipedia/commons/thumb/e/ec/Jaisalmer_Fort_2019.jpg/800px-Jaisalmer_Fort_2019.jpg",
                "File:Jaisalmer Fort",
                "The Golden Fort rising above the golden sands of the Thar Desert in Jaisalmer, Rajasthan",
                "Firoze Edassery",
                "CC BY-SA 3.0",
                "https://creativecommons.org/licenses/by-sa/3.0",
                "https://commons.wikimedia.org/wiki/File:Jaisalmer_Fort_2019.jpg",
                "Photo: Firoze Edassery via Wikimedia Commons, CC BY-SA 3.0"
        ));

        // Agra exclusively receives the Taj Mahal
        CURATED_FALLBACKS.put("agra", new ImageMetadataDto(
                "https://upload.wikimedia.org/wikipedia/commons/1/1d/Taj_Mahal_%28Edited%29.jpeg",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/1/1d/Taj_Mahal_%28Edited%29.jpeg/800px-Taj_Mahal_%28Edited%29.jpeg",
                "File:Taj Mahal (Edited)",
                "The iconic ivory-white marble Taj Mahal in Agra, Uttar Pradesh",
                "Muhammad Mahdi Karim",
                "CC BY-SA 3.0",
                "https://creativecommons.org/licenses/by-sa/3.0",
                "https://commons.wikimedia.org/wiki/File:Taj_Mahal_(Edited).jpeg",
                "Photo: Muhammad Mahdi Karim via Wikimedia Commons, CC BY-SA 3.0"
        ));

        CURATED_FALLBACKS.put("jabalpur", new ImageMetadataDto(
                "https://upload.wikimedia.org/wikipedia/commons/a/a8/Dhuandhar_Falls_in_Bhedaghat.JPG",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a8/Dhuandhar_Falls_in_Bhedaghat.JPG/800px-Dhuandhar_Falls_in_Bhedaghat.JPG",
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
                "https://upload.wikimedia.org/wikipedia/commons/thumb/b/bd/Bee_Falls%2C_Pachmarhi_01.jpg/800px-Bee_Falls%2C_Pachmarhi_01.jpg",
                "File:Bee Falls, Pachmarhi",
                "Cascading waters of Bee Falls in the hill station of Pachmarhi, Madhya Pradesh",
                "Manish.aswani",
                "CC BY-SA 4.0",
                "https://creativecommons.org/licenses/by-sa/4.0",
                "https://commons.wikimedia.org/wiki/File:Bee_Falls%2C_Pachmarhi_01.jpg",
                "Photo: Manish.aswani via Wikimedia Commons, CC BY-SA 4.0"
        ));

        CURATED_FALLBACKS.put("delhi", new ImageMetadataDto(
                "https://upload.wikimedia.org/wikipedia/commons/0/09/India_Gate_in_New_Delhi_03-2016.jpg",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/0/09/India_Gate_in_New_Delhi_03-2016.jpg/800px-India_Gate_in_New_Delhi_03-2016.jpg",
                "File:India Gate in New Delhi",
                "The historic war memorial India Gate illuminated in New Delhi",
                "A.Savin",
                "FAL",
                "http://artlibre.org/licence/lal/en/",
                "https://commons.wikimedia.org/wiki/File:India_Gate_in_New_Delhi_03-2016.jpg",
                "Photo: A.Savin via Wikimedia Commons, Free Art License"
        ));
        CURATED_FALLBACKS.put("new delhi", CURATED_FALLBACKS.get("delhi"));

        CURATED_FALLBACKS.put("mumbai", new ImageMetadataDto(
                "https://upload.wikimedia.org/wikipedia/commons/c/c8/Gateway_of_India_March_2017.jpg",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c8/Gateway_of_India_March_2017.jpg/800px-Gateway_of_India_March_2017.jpg",
                "File:Gateway of India",
                "The monumental Gateway of India on the waterfront of Mumbai, Maharashtra",
                "Ramesh NG",
                "CC BY-SA 2.0",
                "https://creativecommons.org/licenses/by-sa/2.0",
                "https://commons.wikimedia.org/wiki/File:Gateway_of_India_March_2017.jpg",
                "Photo: Ramesh NG via Wikimedia Commons, CC BY-SA 2.0"
        ));

        CURATED_FALLBACKS.put("kolkata", new ImageMetadataDto(
                "https://upload.wikimedia.org/wikipedia/commons/7/72/Victoria_Memorial_Kolkata_sunset.jpg",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/7/72/Victoria_Memorial_Kolkata_sunset.jpg/800px-Victoria_Memorial_Kolkata_sunset.jpg",
                "File:Victoria Memorial Kolkata",
                "Victoria Memorial hall reflecting in gardens during sunset in Kolkata, West Bengal",
                "Deepanjan Ghosh",
                "CC BY-SA 4.0",
                "https://creativecommons.org/licenses/by-sa/4.0",
                "https://commons.wikimedia.org/wiki/File:Victoria_Memorial_Kolkata_sunset.jpg",
                "Photo: Deepanjan Ghosh via Wikimedia Commons, CC BY-SA 4.0"
        ));

        CURATED_FALLBACKS.put("chettinad", new ImageMetadataDto(
                "https://upload.wikimedia.org/wikipedia/commons/0/05/Chettinad_Palace.jpg",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/0/05/Chettinad_Palace.jpg/800px-Chettinad_Palace.jpg",
                "File:Chettinad Palace",
                "Grand heritage mansion and traditional architecture of Chettinad, Tamil Nadu",
                "Naveen K",
                "CC BY-SA 3.0",
                "https://creativecommons.org/licenses/by-sa/3.0",
                "https://commons.wikimedia.org/wiki/File:Chettinad_Palace.jpg",
                "Photo: Naveen K via Wikimedia Commons, CC BY-SA 3.0"
        ));
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

        ImageSearchResponse rawResults = searchImagesRaw(query, limit);
        if (rawResults != null && rawResults.getImages() != null && !rawResults.getImages().isEmpty()) {
            return rawResults;
        }

        log.info("No Wikimedia Commons results for query '{}', checking curated destination or neutral placeholder", query);
        ImageMetadataDto fallback = getCuratedFallback(query);
        if (fallback != null) {
            return new ImageSearchResponse(query, 1, List.of(fallback));
        }

        ImageMetadataDto placeholder = generateNeutralPlaceholder(query, "India");
        return new ImageSearchResponse(query, 1, List.of(placeholder));
    }

    /**
     * Direct query to MediaWiki search API without fallback insertion.
     */
    public ImageSearchResponse searchImagesRaw(String query, int limit) {
        if (query == null || query.trim().isEmpty()) {
            return new ImageSearchResponse(query, 0, Collections.emptyList());
        }

        int queryLimit = Math.max(1, Math.min(limit, 10));
        int thumbWidth = properties.getDefaultThumbnailWidth();

        try {
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
            return new ImageSearchResponse(query, results.size(), results);

        } catch (Exception ex) {
            log.warn("Wikimedia Commons search query failed for '{}': {}", query, ex.getMessage());
            return new ImageSearchResponse(query, 0, Collections.emptyList());
        }
    }

    /**
     * Get the single best image for a destination, with graceful fallback.
     */
    public ImageMetadataDto getBestDestinationImage(String place, String state) {
        return getBestDestinationImage(place, state, Collections.emptyList(), Collections.emptySet());
    }

    /**
     * Relevance-aware destination image selector that searches Wikimedia using landmark keywords,
     * checks metadata relevance, avoids duplicate images across cards, and avoids generic cross-city reuse.
     */
    public ImageMetadataDto getBestDestinationImage(String place, String state, List<String> activities, Set<String> usedImageUrls) {
        if (place == null || place.trim().isEmpty()) {
            return generateNeutralPlaceholder("Scenic India", state != null ? state : "India");
        }

        String normPlace = place.trim();
        List<String> landmarks = getLandmarksForPlace(normPlace, activities);

        // Tier 1: Search Wikimedia Commons for genuine landmark queries
        for (String landmark : landmarks) {
            String query = normPlace + " " + landmark + " filetype:bitmap";
            ImageMetadataDto match = findRelevantUnusedImage(query, normPlace, state, landmark, usedImageUrls);
            if (match != null) {
                log.debug("Found verified landmark photo for {} via query '{}'", normPlace, query);
                return match;
            }
        }

        // Tier 2: Search for Place + State + tourism
        String queryState = normPlace + (state != null && !state.trim().isEmpty() ? " " + state.trim() : "") + " landmark filetype:bitmap";
        ImageMetadataDto matchState = findRelevantUnusedImage(queryState, normPlace, state, null, usedImageUrls);
        if (matchState != null) {
            return matchState;
        }

        // Tier 3: Search for Place + landmark
        String queryPlace = normPlace + " landmark filetype:bitmap";
        ImageMetadataDto matchPlace = findRelevantUnusedImage(queryPlace, normPlace, state, null, usedImageUrls);
        if (matchPlace != null) {
            return matchPlace;
        }

        // Tier 4: Curated destination image if available and not yet used in this batch
        ImageMetadataDto curated = getCuratedFallback(normPlace);
        if (curated != null && (usedImageUrls == null || !isUrlUsed(curated, usedImageUrls))) {
            return curated;
        }

        // Tier 5: Curated destination image even if used (better than unrelated image)
        if (curated != null) {
            return curated;
        }

        // Tier 6: Clean destination-specific neutral SVG placeholder (Never show Taj Mahal for other places)
        log.info("No matching Wikimedia Commons photo found for destination '{}', providing neutral destination placeholder", normPlace);
        return generateNeutralPlaceholder(normPlace, state);
    }

    private ImageMetadataDto findRelevantUnusedImage(String query, String place, String state, String landmark, Set<String> usedImageUrls) {
        try {
            ImageSearchResponse response = searchImagesRaw(query, 6);
            if (response != null && response.getImages() != null) {
                for (ImageMetadataDto candidate : response.getImages()) {
                    if (usedImageUrls != null && isUrlUsed(candidate, usedImageUrls)) {
                        continue;
                    }
                    if (isRelevantImage(candidate, place, state, landmark)) {
                        return candidate;
                    }
                }
            }
        } catch (Exception ex) {
            log.debug("Relevance search failed for '{}': {}", query, ex.getMessage());
        }
        return null;
    }

    private boolean isUrlUsed(ImageMetadataDto image, Set<String> usedUrls) {
        if (usedUrls == null || image == null) return false;
        if (image.getOriginalUrl() != null && usedUrls.contains(image.getOriginalUrl())) return true;
        if (image.getThumbnailUrl() != null && usedUrls.contains(image.getThumbnailUrl())) return true;
        if (image.getUrl() != null && usedUrls.contains(image.getUrl())) return true;
        return false;
    }

    /**
     * Checks if a candidate image is genuinely relevant to the destination or landmark
     * and filters out unrelated maps, flags, diagrams, coats of arms, or non-photos.
     */
    public static boolean isRelevantImage(ImageMetadataDto image, String place, String state, String landmark) {
        if (image == null || image.getUrl() == null || image.getUrl().trim().isEmpty()) {
            return false;
        }

        String title = (image.getTitle() != null ? image.getTitle() : "").toLowerCase();
        String desc = (image.getDescription() != null ? image.getDescription() : "").toLowerCase();
        String combined = title + " " + desc;

        // Reject non-photo graphics, vector diagrams, coats of arms, logos, stamps
        if (combined.contains("flag of") || combined.contains("locator map") || combined.contains("coat of arms")
                || combined.contains("map of") || combined.contains("emblem of") || combined.contains("logo")
                || combined.contains("diagram") || combined.contains(".svg") || title.endsWith(".svg")) {
            return false;
        }

        String normPlace = place != null ? place.toLowerCase().trim() : "";
        String normState = state != null ? state.toLowerCase().trim() : "";
        String normLandmark = landmark != null ? landmark.toLowerCase().trim() : "";

        // Taj Mahal is EXCLUSIVELY for Agra
        if (!normPlace.contains("agra") && (combined.contains("taj mahal") || combined.contains("taj_mahal") || title.contains("taj_mahal"))) {
            return false;
        }

        // Dhuandhar Falls / Bhedaghat is EXCLUSIVELY for Jabalpur / Bhedaghat
        if (!normPlace.contains("jabalpur") && !normPlace.contains("bhedaghat")
                && (combined.contains("dhuandhar") || combined.contains("bhedaghat"))) {
            return false;
        }

        // Check if title or description mentions the place or landmark
        boolean matchesPlace = !normPlace.isEmpty() && combined.contains(normPlace);
        boolean matchesLandmark = !normLandmark.isEmpty() && combined.contains(normLandmark);

        if (matchesPlace || matchesLandmark) {
            return true;
        }

        // State matching only allowed as secondary check if no specific landmark was sought,
        // and image doesn't mention conflicting city names
        if (normLandmark.isEmpty() && !normState.isEmpty() && normState.length() > 3 && combined.contains(normState)) {
            boolean hasOtherCity = combined.contains("delhi") || combined.contains("mumbai") || combined.contains("bengaluru")
                    || combined.contains("kolkata") || combined.contains("chennai") || combined.contains("hyderabad")
                    || combined.contains("jaipur") || combined.contains("varanasi") || combined.contains("agra");
            return !hasOtherCity;
        }

        return false;
    }

    /**
     * Resolves genuine landmarks for a given destination name, also inspecting activity items.
     */
    public static List<String> getLandmarksForPlace(String place, List<String> activities) {
        List<String> list = new ArrayList<>();
        if (place == null) return list;

        String norm = place.toLowerCase().trim();
        for (Map.Entry<String, List<String>> entry : DESTINATION_LANDMARKS.entrySet()) {
            if (norm.contains(entry.getKey()) || entry.getKey().contains(norm)) {
                list.addAll(entry.getValue());
                break;
            }
        }

        // Also extract keywords from activities if available
        if (activities != null) {
            for (String act : activities) {
                if (act != null && act.length() > 3 && act.length() < 30) {
                    list.add(act);
                }
            }
        }

        return list;
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

    /**
     * Looks up curated verified image for a place without returning default cross-destination image.
     */
    public ImageMetadataDto getCuratedFallback(String destination) {
        if (destination == null || destination.trim().isEmpty()) {
            return null;
        }

        String lower = destination.toLowerCase().trim();

        // Exact match first
        if (CURATED_FALLBACKS.containsKey(lower)) {
            return CURATED_FALLBACKS.get(lower);
        }

        // Substring match on curated keys (minimum 4 chars to prevent false substring positives)
        for (Map.Entry<String, ImageMetadataDto> entry : CURATED_FALLBACKS.entrySet()) {
            if (entry.getKey().length() >= 4 && lower.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * Generates a neutral destination-specific SVG data URI placeholder.
     * Guarantees no Taj Mahal or unrelated photo is displayed when a photo is unavailable.
     */
    public static ImageMetadataDto generateNeutralPlaceholder(String place, String state) {
        String safePlace = (place != null && !place.trim().isEmpty()) ? place.trim() : "Scenic India";
        String safeState = (state != null && !state.trim().isEmpty()) ? state.trim() : "Incredible India";

        String svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 1200 800\" width=\"1200\" height=\"800\">" +
                "<defs>" +
                "<linearGradient id=\"bg\" x1=\"0%\" y1=\"0%\" x2=\"100%\" y2=\"100%\">" +
                "<stop offset=\"0%\" stop-color=\"#0d3d38\"/>" +
                "<stop offset=\"50%\" stop-color=\"#115e59\"/>" +
                "<stop offset=\"100%\" stop-color=\"#042f2e\"/>" +
                "</linearGradient>" +
                "<linearGradient id=\"warm\" x1=\"0%\" y1=\"100%\" x2=\"100%\" y2=\"0%\">" +
                "<stop offset=\"0%\" stop-color=\"#d97706\" stop-opacity=\"0.4\"/>" +
                "<stop offset=\"100%\" stop-color=\"#14b8a6\" stop-opacity=\"0\"/>" +
                "</linearGradient>" +
                "</defs>" +
                "<rect width=\"1200\" height=\"800\" fill=\"url(#bg)\"/>" +
                "<rect width=\"1200\" height=\"800\" fill=\"url(#warm)\"/>" +
                "<circle cx=\"960\" cy=\"220\" r=\"130\" fill=\"#fef08a\" opacity=\"0.22\"/>" +
                "<path d=\"M0,580 L240,390 L490,560 L760,330 L1020,530 L1200,420 L1200,800 L0,800 Z\" fill=\"#022c22\" opacity=\"0.7\"/>" +
                "<path d=\"M0,640 L340,480 L620,620 L880,480 L1200,610 L1200,800 L0,800 Z\" fill=\"#064e3b\" opacity=\"0.9\"/>" +
                "<g transform=\"translate(80, 260)\">" +
                "<rect x=\"0\" y=\"0\" width=\"200\" height=\"38\" rx=\"19\" fill=\"#f59e0b\" opacity=\"0.95\"/>" +
                "<text x=\"100\" y=\"24\" fill=\"#1e293b\" font-size=\"16\" font-family=\"sans-serif\" font-weight=\"bold\" text-anchor=\"middle\">AUTHENTIC ESCAPE</text>" +
                "<text x=\"0\" y=\"115\" fill=\"#ffffff\" font-size=\"64\" font-family=\"Georgia,serif\" font-weight=\"bold\">" + escapeXml(safePlace) + "</text>" +
                "<text x=\"0\" y=\"165\" fill=\"#99f6e4\" font-size=\"28\" font-family=\"sans-serif\" font-weight=\"500\" letter-spacing=\"1\">" + escapeXml(safeState).toUpperCase() + ", INDIA</text>" +
                "<text x=\"0\" y=\"215\" fill=\"#ccfbf1\" font-size=\"20\" font-family=\"sans-serif\" opacity=\"0.85\">Curated destination discovery · whereNxt Travel Planner</text>" +
                "</g>" +
                "</svg>";

        String dataUri = "data:image/svg+xml;utf8," + URLEncoder.encode(svg, StandardCharsets.UTF_8).replace("+", "%20");

        return new ImageMetadataDto(
                dataUri,
                dataUri,
                "Scenic " + safePlace + ", " + safeState,
                "Scenic landscape placeholder for " + safePlace + ", " + safeState + ", India",
                "whereNxt Travel Curators",
                "Platform Neutral Artwork",
                "https://creativecommons.org/licenses/by/4.0/",
                "https://wherenxt.travel/destinations/" + safePlace.toLowerCase().replaceAll("[^a-z0-9]", "-"),
                "Scenic artwork for " + safePlace + " by whereNxt Travel Curators"
        );
    }

    private static String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
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
            // Return neutral SVG placeholder
            String fallbackSvg = "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 800 500\"><rect width=\"800\" height=\"500\" fill=\"#0f766e\"/><text x=\"400\" y=\"250\" fill=\"#ffffff\" font-size=\"24\" font-family=\"sans-serif\" text-anchor=\"middle\">whereNxt Travel Destination</text></svg>";
            byte[] svgBytes = fallbackSvg.getBytes(StandardCharsets.UTF_8);
            HttpHeaders respHeaders = new HttpHeaders();
            respHeaders.setContentType(MediaType.valueOf("image/svg+xml"));
            return new ResponseEntity<>(svgBytes, respHeaders, HttpStatus.OK);
        }
    }
}
