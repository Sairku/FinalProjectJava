package com.facebook.enums;

public enum Achievements {
    SWEET_SIGNED_IN( "Sweet & Signed In"),
    PINK_PROFILE( "Pink Profile"),
    SUGAR_RUSH( "Sugar Rush"),
    YOU_ARE_INVITED( "You're invited"),
    BUZZ_STARTED( "Buzz Started"),
    FIRST_HEARTBEAT( "First Heartbeat"),
    VIBE_CREATOR( "Vibe Creator"),
    COMMENT_KING( "Comment King"),
    SOFT_SUPPORTER( "Soft Supporter"),
    TAG_ME_LATER( "Tag Me Later"),
    SWEET_TALKER( "Sweet Talker"),
    AESTHETIC_DROP( "Aesthetic Drop"),
    MAIN_CHARACTER_ENERGY( "Main Character Energy"),
    WHIMSICAL_WONDER( "Whimsical Wonder"),
    KIND_SOUL( "Kind Soul"),
    BUZZLIGHT_STAR( "Buzzlight Star"),
    NIGHT_SCROLLER( "Night Scroller"),
    VANISHED_AND_REBORN( "Vanished & Reborn"),
    TREND_STARTER( "Trend Starter"),
    BUZZ_ROYALTY( "Buzz Royalty"),
    BEE_BABY( "Bee Baby"),
    GOLDEN_BUZZ( "Golden Buzz"),
    BUZZ_GIFTER( "Buzz Gifter"),
    STYLE_KING( "Style King"),
    PREMIUM_PLAYER( "Premium Player"),;

    private final String achievementInString;

    Achievements(String achievementInString) {
        this.achievementInString = achievementInString;
    }

    @Override
    public String toString() {
        return achievementInString;
    }
}
