package com.network;

public class OffensiveWords {
    public static String[] banWords = {
            // Bullying-related words or phrases
            "Harassment", "Intimidation", "Cyberbullying", "Verbal abuse", "Physical aggression", "Social exclusion", "Teasing", "Threats", "Coercion", "Victimization", "Rumors", "Name-calling", "Taunting", "Discrimination", "Bullycide", "Targeting", "Bystander effect", "Aggressive behavior", "Abuse of power", "Repeated torment",
            // Violence or threats of violence
            "Assault", "Battery", "Kill", "Murder", "Aggravated assault", "Homicide", "Threatening behavior", "Physical altercation", "Brutality", "Menacing", "Attack", "Brutal force", "Aggression", "Violent behavior", "Fighting", "Aggressive threats", "Hostility", "Provocation", "Endangerment", "Feud", "Harmful behavior", "Injurious actions",
            // Drug-related terms
            "Substance abuse", "Narcotics", "Addiction", "Drug dependency", "Illegal substances", "Recreational drugs", "Intoxication", "Marijuana use", "Cocaine abuse", "Heroin addiction", "Methamphetamine use", "Prescription drug misuse", "Hallucinogens", "Opioid abuse", "Substance misuse", "Psychedelics", "Stimulants", "Sedatives", "Designer drugs", "Overdose",
            // Alcohol-related terms
            "Alcoholism", "Binge drinking", "Intoxication", "Drinking problem", "Excessive drinking", "Liquor abuse", "Alcohol dependency", "Alcoholic beverages", "Heavy drinking", "Alcohol poisoning", "Drunkenness", "Alcohol addiction", "Underage drinking", "Alcopop", "Spirits consumption", "Beer guzzling", "Wine drinking", "Alcohol tolerance", "Cocktail hour", "Booze consumption",
            // Terms related to self-harm or suicide
            "Self-injury", "Self-mutilation", "Suicidal ideation", "Suicide attempt", "Cutting", "Self-destructive behavior", "Self-harming", "Suicide prevention", "Suicidal tendencies", "Suicidal", "Self-inflicted wounds", "Suicide intervention", "Suicidal thoughts", "Suicidal behavior", "Suicidal impulses", "Suicide hotline", "Self-harm awareness", "Suicidal gestures", "Suicidal feelings", "Self-harm recovery",
            // Insensitive remarks about disabilities or mental health conditions
            "Ableism", "Mental health stigma", "Derogatory comments", "Disparaging remarks", "Insensitive language", "Mockery of disabilities", "Prejudiced attitudes", "Offensive labeling", "Belittling disabilities", "Stigmatizing language", "Disability discrimination", "Ridicule of mental health", "Hurtful comments", "Stereotyping disabilities", "Mental health shaming", "Ignorant remarks", "Offensive portrayals", "Discriminatory language", "Disrespectful comments", "Dehumanizing language",
            // Disparaging remarks about socio-economic status
            "Classism", "Socio-economic discrimination", "Economic disparagement", "Prejudice against poverty", "Wealth shaming", "Income-based ridicule", "Social hierarchy insults", "Economic marginalization", "Stereotyping social status", "Discrimination based on wealth", "Poverty stigma", "Social class prejudice", "Disrespect for economic status", "Socio-economic belittlement", "Economic segregation", "Affluence mockery", "Social standing bias", "Economic exclusion", "Socio-economic slurs", "Poverty derogation",
            // Offensive slang or expressions about body parts or bodily functions
            "Vulgar language", "Crude remarks", "Obscene slang", "Profanity", "Lewd comments", "Indecent language", "Gross expressions", "Rude phrases", "Offensive terms", "Insulting slang", "Raunchy language", "Disrespectful phrases", "Off-color remarks", "Inappropriate language", "Indelicate expressions", "Tasteless language", "Crass comments", "Improper slang", "Disparaging phrases", "Inflammatory language"};

    public static String is_offensive(String str) {
        for (String word : banWords) {
            if (str.toLowerCase().contains(word.toLowerCase())) {
                return "NSFW_CONTENT";
            }
        }
        return str;
    }

    public static StringBuilder righteous_word(String text) {
        String[] words = text.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            result.append(is_offensive(word)).append(" ");
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println(righteous_word("initiate_client_message"));
        System.out.println(righteous_word("I am a killer person"));
        System.out.println(righteous_word("I am a good person"));
    }
}
