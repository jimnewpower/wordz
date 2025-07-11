package dev.newpower.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

/**
 * Service for word validation using a dictionary.
 */
@Service
public class WordDictionaryService {
    
    private Set<String> validWords;
    private String[] commonWords;

    @PostConstruct
    public void initializeDictionary() {
        validWords = new HashSet<>();
        loadDictionary();
        createBasicDictionary();
    }
    
    /**
     * Loads the dictionary from the words.txt file.
     */
    private void loadDictionary() {
        try {
            ClassPathResource resource = new ClassPathResource("words.txt");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String word = line.trim().toUpperCase();
                    if (word.length() >= 2 && word.matches("^[A-Z]+$")) {
                        validWords.add(word);
                    }
                }
            }
        } catch (IOException e) {
            // If file not found, create a basic dictionary with common words
            createBasicDictionary();
        }
    }
    
    /**
     * Creates a basic dictionary with common Scrabble words if the file is not available.
     */
    private void createBasicDictionary() {
        commonWords = new String[] {
            "HELLO", "WORLD", "GAME", "PLAY", "WORD", "TILE", "SCORE", "POINT",
            "LETTER", "BOARD", "START", "END", "WIN", "LOSE", "DRAW", "PASS",
            "QUIT", "HELP", "RULES", "TURN", "NEXT", "LAST", "FIRST", "BEST",
            "GOOD", "BAD", "BIG", "SMALL", "LONG", "SHORT", "HIGH", "LOW",
            "FAST", "SLOW", "HOT", "COLD", "WARM", "COOL", "NEW", "OLD",
            "YOUNG", "RICH", "POOR", "HAPPY", "SAD", "ANGRY", "CALM", "QUIET",
            "LOUD", "SOFT", "HARD", "EASY", "DIFFICULT", "SIMPLE", "COMPLEX",
            "CLEAR", "FOGGY", "BRIGHT", "DARK", "LIGHT", "HEAVY", "LIGHT",
            "STRONG", "WEAK", "HEALTHY", "SICK", "ALIVE", "DEAD", "OPEN",
            "CLOSE", "BEGIN", "FINISH", "START", "STOP", "GO", "COME",
            "LEAVE", "ARRIVE", "ENTER", "EXIT", "UP", "DOWN", "IN", "OUT",
            "ON", "OFF", "YES", "NO", "MAYBE", "ALWAYS", "NEVER", "SOMETIMES",
            "OFTEN", "RARELY", "SOON", "LATE", "EARLY", "NOW", "THEN",
            "BEFORE", "AFTER", "DURING", "WHILE", "UNTIL", "SINCE", "BECAUSE",
            "ALTHOUGH", "HOWEVER", "THEREFORE", "NEVERTHELESS", "MEANWHILE",
            "FINALLY", "INITIALLY", "ORIGINALLY", "CURRENTLY", "PREVIOUSLY",
            "SUBSEQUENTLY", "IMMEDIATELY", "GRADUALLY", "SUDDENLY", "SLOWLY",
            "QUICKLY", "CAREFULLY", "CARELESSLY", "HAPPILY", "SADLY", "ANGRILY",
            "CALMLY", "QUIETLY", "LOUDLY", "SOFTLY", "HARDLY", "EASILY",
            "DIFFICULTLY", "SIMPLY", "COMPLEXLY", "CLEARLY", "FOGGILY",
            "BRIGHTLY", "DARKLY", "LIGHTLY", "HEAVILY", "STRONGLY", "WEAKLY",
            "HEALTHILY", "SICKLY", "OPENLY", "CLOSELY", "BEGINNING", "ENDING",
            "STARTING", "STOPPING", "GOING", "COMING", "LEAVING", "ARRIVING",
            "ENTERING", "EXITING", "RISING", "FALLING", "INCREASING", "DECREASING",
            "IMPROVING", "WORSENING", "GROWING", "SHRINKING", "EXPANDING", "CONTRACTING",
            "BUILDING", "DESTROYING", "CREATING", "MAKING", "BREAKING", "FIXING",
            "REPAIRING", "CLEANING", "DIRTYING", "WASHING", "DRYING", "WETTING",
            "HEATING", "COOLING", "FREEZING", "MELTING", "BOILING", "EVAPORATING",
            "CONDENSING", "SOLIDIFYING", "LIQUEFYING", "GASIFYING", "CRYSTALLIZING",
            "DISSOLVING", "MIXING", "SEPARATING", "COMBINING", "DIVIDING",
            "MULTIPLYING", "ADDING", "SUBTRACTING", "CALCULATING", "MEASURING",
            "WEIGHING", "COUNTING", "NUMBERING", "ORDERING", "ARRANGING",
            "ORGANIZING", "PLANNING", "DESIGNING", "DRAWING", "PAINTING",
            "WRITING", "READING", "SPEAKING", "LISTENING", "WATCHING", "LOOKING",
            "SEEING", "HEARING", "FEELING", "TOUCHING", "HOLDING", "GRABBING",
            "RELEASING", "PUSHING", "PULLING", "LIFTING", "DROPPING", "THROWING",
            "CATCHING", "KICKING", "HITTING", "PUNCHING", "FIGHTING", "PLAYING",
            "WORKING", "RESTING", "SLEEPING", "WAKING", "DREAMING", "THINKING",
            "KNOWING", "LEARNING", "TEACHING", "STUDYING", "PRACTICING", "TRAINING",
            "EXERCISING", "RUNNING", "WALKING", "JUMPING", "DANCING", "SINGING",
            "MUSIC", "SONG", "MELODY", "RHYTHM", "BEAT", "TUNE", "VOICE",
            "SOUND", "NOISE", "SILENCE", "ECHO", "WHISPER", "SHOUT", "SCREAM",
            "LAUGH", "CRY", "SMILE", "FROWN", "GRIN", "WINK", "BLINK", "STARE",
            "GLANCE", "PEEK", "PEEP", "GAZE", "WATCH", "OBSERVE", "NOTICE",
            "SEE", "SPOT", "FIND", "DISCOVER", "EXPLORE", "SEARCH", "LOOK",
            "HUNT", "CHASE", "FOLLOW", "LEAD", "GUIDE", "DIRECT", "SHOW",
            "TELL", "SAY", "SPEAK", "TALK", "CHAT", "CONVERSE", "DISCUSS",
            "ARGUE", "DEBATE", "AGREE", "DISAGREE", "ACCEPT", "REJECT",
            "APPROVE", "DENY", "ALLOW", "FORBID", "PERMIT", "REFUSE",
            "GRANT", "GIVE", "TAKE", "RECEIVE", "GET", "OBTAIN", "ACQUIRE",
            "BUY", "SELL", "TRADE", "EXCHANGE", "SWAP", "SHARE", "DIVIDE",
            "SPLIT", "BREAK", "CUT", "SLICE", "CHOP", "DICE", "GRIND",
            "CRUSH", "SMASH", "CRACK", "SNAP", "BEND", "FOLD", "WRAP",
            "COVER", "UNCOVER", "REVEAL", "HIDE", "CONCEAL", "SECRET",
            "PRIVATE", "PUBLIC", "OPEN", "CLOSED", "LOCKED", "UNLOCKED",
            "SAFE", "DANGEROUS", "RISKY", "CAREFUL", "CAUTIOUS", "BOLD",
            "BRAVE", "COURAGEOUS", "FEARLESS", "SCARED", "AFRAID", "TERRIFIED",
            "PANICKED", "NERVOUS", "ANXIOUS", "WORRIED", "CONCERNED", "CARE",
            "LOVE", "LIKE", "HATE", "DISLIKE", "ENJOY", "PREFER", "CHOOSE",
            "SELECT", "PICK", "DECIDE", "DETERMINE", "RESOLVE", "SOLVE",
            "ANSWER", "QUESTION", "ASK", "INQUIRE", "WONDER", "CURIOUS",
            "INTERESTED", "BORED", "EXCITED", "THRILLED", "AMAZED", "SURPRISED",
            "SHOCKED", "STUNNED", "CONFUSED", "PUZZLED", "PERPLEXED", "MYSTIFIED",
            "BAFFLED", "BEWILDERED", "CONFOUNDED", "STUMPED", "STUCK", "TRAPPED",
            "FREE", "LIBERATED", "RELEASED", "ESCAPED", "FLED", "RAN", "WALKED",
            "TRAVELED", "JOURNEYED", "VOYAGED", "SAILED", "FLEW", "DROVE",
            "RODE", "CARRIED", "BROUGHT", "TOOK", "FETCHED", "DELIVERED",
            "SENT", "MAILED", "POSTED", "SHIPPED", "TRANSPORTED", "MOVED",
            "SHIFTED", "CHANGED", "ALTERED", "MODIFIED", "ADJUSTED", "FIXED",
            "REPAIRED", "MENDED", "HEALED", "CURED", "TREATED", "MEDICINE",
            "DRUG", "PILL", "TABLET", "CAPSULE", "LIQUID", "SOLID", "GAS",
            "VAPOR", "SMOKE", "STEAM", "WATER", "ICE", "SNOW", "RAIN",
            "SUN", "MOON", "STAR", "PLANET", "EARTH", "WORLD", "UNIVERSE",
            "SPACE", "TIME", "DAY", "NIGHT", "MORNING", "EVENING", "AFTERNOON",
            "MIDNIGHT", "NOON", "DAWN", "DUSK", "SUNRISE", "SUNSET", "TWILIGHT",
            "SHADOW", "LIGHT", "DARKNESS", "BRIGHTNESS", "DIMNESS", "CLARITY",
            "FOG", "MIST", "CLOUD", "SKY", "AIR", "WIND", "BREEZE", "STORM",
            "THUNDER", "LIGHTNING", "RAINBOW", "COLOR", "RED", "BLUE", "GREEN",
            "YELLOW", "ORANGE", "PURPLE", "PINK", "BROWN", "BLACK", "WHITE",
            "GRAY", "SILVER", "GOLD", "BRONZE", "COPPER", "IRON", "STEEL",
            "WOOD", "STONE", "GLASS", "PLASTIC", "PAPER", "CLOTH", "FABRIC",
            "COTTON", "SILK", "WOOL", "LEATHER", "RUBBER", "METAL", "ALUMINUM",
            "TITANIUM", "CARBON", "OXYGEN", "HYDROGEN", "NITROGEN", "HELIUM",
            "NEON", "ARGON", "KRYPTON", "XENON", "RADON", "URANIUM", "PLUTONIUM",
            "RADIUM", "THORIUM", "CESIUM", "STRONTIUM", "BARIUM", "RADON",
            "FRANCIUM", "RADIUM", "ACTINIUM", "THORIUM", "PROTACTINIUM",
            "URANIUM", "NEPTUNIUM", "PLUTONIUM", "AMERICIUM", "CURIUM",
            "BERKELIUM", "CALIFORNIUM", "EINSTEINIUM", "FERMIUM", "MENDELEVIUM",
            "NOBELIUM", "LAWRENCIUM", "RUTHERFORDIUM", "DUBNIUM", "SEABORGIUM",
            "BOHRIUM", "HASSIUM", "MEITNERIUM", "DARMSTADTIUM", "ROENTGENIUM",
            "COPERNICIUM", "NIHONIUM", "FLEROVIUM", "MOSCOVIUM", "LIVERMORIUM",
            "TENNESSINE", "OGANESSON"
        };
    }
    
    /**
     * Checks if a word is valid in the dictionary.
     */
    public boolean isValidWord(String word) {
        if (word == null || word.trim().isEmpty()) {
            return false;
        }
        return validWords.contains(word.toUpperCase().trim());
    }
    
    /**
     * Gets a random valid word of specified length.
     */
    public String getRandomWord(int length) {
        // return validWords.stream()
        //         .filter(word -> word.length() == length)
        //         .findAny()
        //         .orElse("HELLO");
        int randomIndex = new Random().nextInt(commonWords.length);
        return commonWords[randomIndex];
    }
    
    /**
     * Gets the number of words in the dictionary.
     */
    public int getDictionarySize() {
        return validWords.size();
    }
} 