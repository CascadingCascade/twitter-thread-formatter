package com.github.cascadingcascade.twitterthreadformatter;

import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("Set tweet width:");
        Scanner scanner = new Scanner(System.in);
        int width = scanner.nextInt();
        scanner.nextLine();
        thread newThread = new thread(width);
        System.out.println("Input text to be formatted, input an empty line to stop:");
        StringBuilder builder = new StringBuilder(560);
        String line = scanner.nextLine();
        while (!line.isEmpty()) {
            builder.append(line);
            line = scanner.nextLine();
        }
        if (builder.length() <= thread.maxTweetLen) {
            System.out.println("You don't need a thread for that idiot");
            System.exit(418);
        }
        newThread.fill(builder.toString());
        System.out.println(newThread);
    }
}

class thread {

    public static final int maxTweetLen = 280;

    private List<StringBuilder> rawContent;
    private List<StringBuilder> formattedContent = new ArrayList<>();
    int width;
    int totalHeight = -1;
    int contentLen = -1;
    public thread(int width) {
        checkWidth(width);
        this.width = width;
        this.rawContent = new ArrayList<>(width);
        for (int i = 0; i < width; i++) {
            rawContent.add(i, new StringBuilder());
        }
    }
    private static void checkWidth(int w) {
        if (w > 70 || w <= 0)
            throw new RuntimeException("WTH are you doing");
    }

    private static final CharsetEncoder charsetEncoder = StandardCharsets.US_ASCII.newEncoder();
    // For simplicity, we assume that ASCII characters count as a character
    // While non-ASCII count as two characters
    // This is not exactly true: https://developer.twitter.com/en/docs/counting-characters
    // But I don't care
    private static boolean isSingleLen(char ch) {
        return charsetEncoder.canEncode(ch);
    }

    public void fill(String string) {
        if (string.isEmpty())
            throw new RuntimeException("WTH are you doing");

        totalHeight = string.length() / width +
                (string.length() % width == 0 ? 0 : 1);
        contentLen = string.length();

        int index = 0;
        try {
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < totalHeight; j++) {
                    rawContent.get(i).append(string.charAt(index++));
                }
            }
        } catch (StringIndexOutOfBoundsException ignored) {
            // What kind of programmer use this sort of hack?
            // At least this way we won't have to check if index reached string.length right?
            int lastColLen = rawContent.getLast().length();
            for (int i = 0; i < totalHeight - lastColLen; i++) {
                rawContent.getLast().append(' ');
            }
        }

        format();
    }

    private void format() {
        if (totalHeight < 0)
            throw new RuntimeException("WTH are you doing");

        formattedContent.clear();
        int maxHeightPerTweet = (maxTweetLen / 2) / width;
        int tweetsNeeded = totalHeight / maxHeightPerTweet +
                (totalHeight % maxHeightPerTweet == 0 ? 0 : 1);
        for (int i = 0; i < tweetsNeeded; i++) {
            formattedContent.add(new StringBuilder(maxTweetLen));
        }

        StringBuilder currentTweet;
        for (int i = 0; i < tweetsNeeded; i++) {
            currentTweet = formattedContent.get(i);
            int offset = maxHeightPerTweet * i;
            try {
                for (int j = 0; j < maxHeightPerTweet; j++) {
                    for (int k = 0; k < width; k++) {
                        char currentChar = rawContent.get(k).charAt(j + offset);
                        // For now, we just add a padding to single length characters
                        // I may or may not introduce a 'compact mode' for texts
                        // That only includes single length characters
                        currentTweet.append(currentChar);
                        if (isSingleLen(currentChar))
                            currentTweet.append(' ');
                    }
                    currentTweet.append('\n');
                }

            } catch (StringIndexOutOfBoundsException ignored) {

            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < formattedContent.size(); i++) {
            builder.append("Tweet ").append(i + 1).append(":\n");
            builder.append(formattedContent.get(i));
        }
        return builder.toString();
    }
}