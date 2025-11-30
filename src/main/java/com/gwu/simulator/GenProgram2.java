package com.gwu.simulator;

import java.util.*;

public class GenProgram2 {
    static int W(int op, int r, int ix, int i, int addr) {
        return (op << 10) | (r << 8) | (ix << 6) | (i << 5) | (addr & 0x1F);
    }

    static int AIR(int r, int imm) {
        return (6 << 10) | (r << 8) | (imm & 0xFF);
    }

    static int SIR(int r, int imm) {
        return (7 << 10) | (r << 8) | (imm & 0xFF);
    }

    static int INW(int r, int dev) {
        return (61 << 10) | (r << 6) | (dev & 0x1F);
    }

    static int OUTW(int r, int dev) {
        return (62 << 10) | (r << 6) | (dev & 0x1F);
    }

    static int SOBW(int r, int ix, int i, int addr) {
        return (16 << 10) | (r << 8) | (ix << 6) | (i << 5) | (addr & 0x1F);
    }

    static int AMRW(int r, int ix, int i, int addr) {
        return (4 << 10) | (r << 8) | (ix << 6) | (i << 5) | (addr & 0x1F);
    }

    static int SMRW(int r, int ix, int i, int addr) {
        return (5 << 10) | (r << 8) | (ix << 6) | (i << 5) | (addr & 0x1F);
    }

    static int NOTW(int r) {
        return (25 << 10) | (r << 8);
    }
    static int JZ(int r, int ix, int i, int addr) {
        return W(10, r, ix, i, addr);
    }
    static int JNE(int r, int ix, int i, int addr) {
        return W(11, r, ix, i, addr);
    }
    static int JCC(int ccBit, int ix, int i, int addr) {
        return W(12, ccBit, ix, i, addr);
    }
    static int JMA(int ix, int i, int addr) {
        return W(13, 0, ix, i, addr);
    }

    static String toOct(int v, int width) {
        String s = Integer.toOctalString(v & 0xFFFF);
        while (s.length() < width) s = "0" + s;
        return s;
    }

    static int emitString(List<Item> items, int startAddr, String s, String commentPrefix) {
        int a = startAddr;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            int word = (int) ch;   // ASCII in low 8 bits
            items.add(new Item(a, word, commentPrefix + " '" + ch + "'"));
            a++;
        }
        return a;
    }

    public static void main(String[] args) {
        List<Item> items = new ArrayList<>();
        // 1..6: constants
        items.add(new Item(1, 6,          "N_SENT = 6"));
        items.add(new Item(2, 0500,       "PAR_BASE = 0o500"));  // paragraph base
        items.add(new Item(3, 0700,       "WORD_BUF = 0o700"));  // input word buffer base
        items.add(new Item(4, (int) ' ',  "SPACE_CH = ' '"));
        items.add(new Item(5, (int) '.',  "DOT_CH   = '.'"));
        items.add(new Item(6, 0,          "ZERO = 0"));
        int PTR_PARA       = 010;  // pointer into paragraph (holds an address)
        int PTR_WORD       = 011;  // pointer into word buffer (holds an address)
        int WORD_LEN       = 012;  // length of input word
        int SENT_NO        = 013;  // current sentence #
        int WORD_NO        = 014;  // current word # in sentence
        int FOUND          = 015;  // flag 0/1
        int MATCH_SENT_NO  = 016;  // where we store match sentence #
        int MATCH_WORD_NO  = 017;  // where we store match word #

        items.add(new Item(PTR_PARA,      0, "PTR_PARA"));
        items.add(new Item(PTR_WORD,      0, "PTR_WORD"));
        items.add(new Item(WORD_LEN,      0, "WORD_LEN"));
        items.add(new Item(SENT_NO,       0, "SENT_NO"));
        items.add(new Item(WORD_NO,       0, "WORD_NO"));
        items.add(new Item(FOUND,         0, "FOUND"));
        items.add(new Item(MATCH_SENT_NO, 0, "MATCH_SENT_NO"));
        items.add(new Item(MATCH_WORD_NO, 0, "MATCH_WORD_NO"));
        int TMP_CHAR = 020;
        int TMP_R1   = 021;
        int TMP_PARA = 022;
        int TMP_WORD = 023;
        items.add(new Item(TMP_CHAR, 0, "TMP_CHAR"));
        items.add(new Item(TMP_R1,   0, "TMP_R1"));
        items.add(new Item(TMP_PARA, 0, "TMP_PARA"));
        items.add(new Item(TMP_WORD, 0, "TMP_WORD"));
        int LAST_DELIM = 024;
        items.add(new Item(LAST_DELIM, 1, "LAST_DELIM (1=at boundary,0=inside word)"));
        int ADDR_PRINT_LOOP    = 025;
        int ADDR_DONE_PRINT    = 026;
        int ADDR_READ_LOOP     = 027;
        int ADDR_DONE_READ     = 030;
        int ADDR_SEARCH_LOOP   = 031;
        int ADDR_MATCH_FOUND   = 032;
        int ADDR_SEARCH_DONE   = 033;
        int ADDR_MATCH_LOOP    = 034;
        int ADDR_DOT_HANDLER   = 035;
        int ADDR_MISMATCH      = 036;
        int ADDR_SPACE_HANDLER = 037;
        items.add(new Item(ADDR_PRINT_LOOP,    0, "ADDR_PRINT_LOOP"));
        items.add(new Item(ADDR_DONE_PRINT,    0, "ADDR_DONE_PRINT"));
        items.add(new Item(ADDR_READ_LOOP,     0, "ADDR_READ_LOOP"));
        items.add(new Item(ADDR_DONE_READ,     0, "ADDR_DONE_READ"));
        items.add(new Item(ADDR_SEARCH_LOOP,   0, "ADDR_SEARCH_LOOP"));
        items.add(new Item(ADDR_MATCH_FOUND,   0, "ADDR_MATCH_FOUND"));
        items.add(new Item(ADDR_SEARCH_DONE,   0, "ADDR_SEARCH_DONE"));
        items.add(new Item(ADDR_MATCH_LOOP,    0, "ADDR_MATCH_LOOP"));
        items.add(new Item(ADDR_DOT_HANDLER,   0, "ADDR_DOT_HANDLER"));
        items.add(new Item(ADDR_MISMATCH,      0, "ADDR_MISMATCH"));
        items.add(new Item(ADDR_SPACE_HANDLER, 0, "ADDR_SPACE_HANDLER"));
        int PAR_BASE = 0500; // matches cell 2
        int a = PAR_BASE;
        a = emitString(items, a, "THIS IS SENTENCE ONE.",    "PAR");
        a = emitString(items, a, " THIS IS SENTENCE TWO.",   "PAR");
        a = emitString(items, a, " THIS IS SENTENCE THREE.", "PAR");
        a = emitString(items, a, " THIS IS SENTENCE FOUR.",  "PAR");
        a = emitString(items, a, " THIS IS SENTENCE FIVE.",  "PAR");
        a = emitString(items, a, " THIS IS SENTENCE SIX.",   "PAR");
        items.add(new Item(a, 0, "PAR_END (0 terminator)"));
        a++;
        int CODE_BASE = 0100;
        int pc = CODE_BASE;
        items.add(new Item(pc, W(1, 0, 0, 0, 2), "LDR R0,PAR_BASE")); pc++;
        items.add(new Item(pc, W(2, 0, 0, 0, PTR_PARA), "STR R0,PTR_PARA")); pc++;
        items.add(new Item(pc, W(1, 0, 0, 0, 6), "LDR R0,ZERO")); pc++;
        items.add(new Item(pc, AIR(0, 1), "AIR R0,1 ; SENT_NO=1")); pc++;
        items.add(new Item(pc, W(2, 0, 0, 0, SENT_NO), "STR R0,SENT_NO")); pc++;
        items.add(new Item(pc, W(1, 0, 0, 0, 6), "LDR R0,ZERO")); pc++;
        items.add(new Item(pc, W(2, 0, 0, 0, WORD_NO), "STR R0,WORD_NO")); pc++;
        items.add(new Item(pc, W(2, 0, 0, 0, FOUND),   "STR R0,FOUND")); pc++;
        items.add(new Item(pc, AIR(0, 1), "AIR R0,1 ; LAST_DELIM=1")); pc++;
        items.add(new Item(pc, W(2, 0, 0, 0, LAST_DELIM), "STR R0,LAST_DELIM")); pc++;
        int printLoop = pc;
        items.add(new Item(pc, W(1, 0, 0, 1, PTR_PARA), "LDR R0,(PTR_PARA) ; char")); pc++;
        items.add(new Item(pc, JZ(0, 0, 1, ADDR_DONE_PRINT), "JZ R0,@ADDR_DONE_PRINT")); pc++;
        items.add(new Item(pc, OUTW(0, 2), "OUT R0,2 ; print ASCII char")); pc++;
        items.add(new Item(pc, W(1, 1, 0, 0, PTR_PARA), "LDR R1,PTR_PARA")); pc++;
        items.add(new Item(pc, AIR(1, 1), "AIR R1,1 ; PTR_PARA++")); pc++;
        items.add(new Item(pc, W(2, 1, 0, 0, PTR_PARA), "STR R1,PTR_PARA")); pc++;
        items.add(new Item(pc, JMA(0, 1, ADDR_PRINT_LOOP), "JMA @ADDR_PRINT_LOOP")); pc++;

        int donePrint = pc;
        items.add(new Item(pc, W(1, 0, 0, 0, 3), "LDR R0,WORD_BUF")); pc++;
        items.add(new Item(pc, W(2, 0, 0, 0, PTR_WORD), "STR R0,PTR_WORD")); pc++;
        items.add(new Item(pc, W(1, 0, 0, 0, 6), "LDR R0,ZERO")); pc++;
        items.add(new Item(pc, W(2, 0, 0, 0, WORD_LEN), "STR R0,WORD_LEN")); pc++;

        int readLoop = pc;
        items.add(new Item(pc, INW(0, 3), "IN R0,3 ; read ASCII char")); pc++;
        items.add(new Item(pc, JZ(0, 0, 1, ADDR_DONE_READ), "JZ R0,@ADDR_DONE_READ")); pc++;
        items.add(new Item(pc, W(2, 0, 0, 1, PTR_WORD), "STR R0,(PTR_WORD)")); pc++;
        items.add(new Item(pc, W(1, 1, 0, 0, PTR_WORD), "LDR R1,PTR_WORD")); pc++;
        items.add(new Item(pc, AIR(1, 1), "AIR R1,1 ; PTR_WORD++")); pc++;
        items.add(new Item(pc, W(2, 1, 0, 0, PTR_WORD), "STR R1,PTR_WORD")); pc++;
        items.add(new Item(pc, W(1, 0, 0, 0, WORD_LEN), "LDR R0,WORD_LEN")); pc++;
        items.add(new Item(pc, AIR(0, 1), "AIR R0,1 ; WORD_LEN++")); pc++;
        items.add(new Item(pc, W(2, 0, 0, 0, WORD_LEN), "STR R0,WORD_LEN")); pc++;
        items.add(new Item(pc, JMA(0, 1, ADDR_READ_LOOP), "JMA @ADDR_READ_LOOP")); pc++;

        int doneRead = pc;
        items.add(new Item(pc, W(1, 1, 0, 0, PTR_WORD), "LDR R1,PTR_WORD")); pc++;
        items.add(new Item(pc, W(1, 0, 0, 0, 6), "LDR R0,ZERO")); pc++;
        items.add(new Item(pc, W(2, 0, 0, 1, PTR_WORD), "STR R0,(PTR_WORD) ; word terminator")); pc++;
        items.add(new Item(pc, W(1, 0, 0, 0, 2), "LDR R0,PAR_BASE")); pc++;
        items.add(new Item(pc, W(2, 0, 0, 0, PTR_PARA), "STR R0,PTR_PARA")); pc++;
        items.add(new Item(pc, W(1, 0, 0, 0, 6), "LDR R0,ZERO")); pc++;
        items.add(new Item(pc, AIR(0, 1), "AIR R0,1 ; SENT_NO=1")); pc++;
        items.add(new Item(pc, W(2, 0, 0, 0, SENT_NO), "STR R0,SENT_NO")); pc++;

        items.add(new Item(pc, W(1, 0, 0, 0, 6), "LDR R0,ZERO")); pc++;
        items.add(new Item(pc, W(2, 0, 0, 0, WORD_NO), "STR R0,WORD_NO")); pc++;
        items.add(new Item(pc, W(2, 0, 0, 0, FOUND),   "STR R0,FOUND")); pc++;
        items.add(new Item(pc, AIR(0, 1), "AIR R0,1 ; LAST_DELIM=1")); pc++;
        items.add(new Item(pc, W(2, 0, 0, 0, LAST_DELIM), "STR R0,LAST_DELIM")); pc++;
        int searchLoop = pc;
        items.add(new Item(pc, W(1, 0, 0, 1, PTR_PARA), "LDR R0,(PTR_PARA) ; char")); pc++;
        items.add(new Item(pc, JZ(0, 0, 1, ADDR_SEARCH_DONE), "JZ R0,@ADDR_SEARCH_DONE")); pc++;
        items.add(new Item(pc, W(2, 0, 0, 0, TMP_CHAR), "STR R0,TMP_CHAR")); pc++;
        items.add(new Item(pc, W(1, 1, 0, 0, 4), "LDR R1,SPACE_CH")); pc++;
        items.add(new Item(pc, SMRW(1, 0, 0, TMP_CHAR), "SMR R1,TMP_CHAR ; R1 = ' ' - ch")); pc++;
        items.add(new Item(pc, JZ(1, 0, 1, ADDR_SPACE_HANDLER),
                           "JZ R1,@ADDR_SPACE_HANDLER ; space")); pc++;
        items.add(new Item(pc, W(1, 1, 0, 0, 5), "LDR R1,DOT_CH")); pc++;
        items.add(new Item(pc, SMRW(1, 0, 0, TMP_CHAR), "SMR R1,TMP_CHAR ; R1 = '.' - ch")); pc++;
        items.add(new Item(pc, JZ(1, 0, 1, ADDR_DOT_HANDLER),
                           "JZ R1,@ADDR_DOT_HANDLER ; '.' sentence boundary")); pc++;
        items.add(new Item(pc, W(1, 0, 0, 0, LAST_DELIM), "LDR R0,LAST_DELIM")); pc++;
        items.add(new Item(pc, JZ(0, 0, 1, ADDR_MISMATCH),
                           "JZ R0,@ADDR_MISMATCH ; if 0, inside word -> no new word")); pc++;
        items.add(new Item(pc, W(1, 2, 0, 0, WORD_NO), "LDR R2,WORD_NO")); pc++;
        items.add(new Item(pc, AIR(2, 1), "AIR R2,1 ; WORD_NO++")); pc++;
        items.add(new Item(pc, W(2, 2, 0, 0, WORD_NO), "STR R2,WORD_NO")); pc++;
        items.add(new Item(pc, W(1, 0, 0, 0, 6), "LDR R0,ZERO")); pc++;
        items.add(new Item(pc, W(2, 0, 0, 0, LAST_DELIM), "STR R0,LAST_DELIM")); pc++;
        items.add(new Item(pc, W(1, 1, 0, 0, PTR_PARA), "LDR R1,PTR_PARA")); pc++;
        items.add(new Item(pc, W(2, 1, 0, 0, TMP_PARA), "STR R1,TMP_PARA")); pc++;
        items.add(new Item(pc, W(1, 1, 0, 0, 3), "LDR R1,WORD_BUF")); pc++;
        items.add(new Item(pc, W(2, 1, 0, 0, TMP_WORD), "STR R1,TMP_WORD")); pc++;
        int matchLoopPC = pc;
        items.add(new Item(pc, W(1, 0, 0, 1, TMP_PARA), "LDR R0,(TMP_PARA) ; ch_p")); pc++;
        items.add(new Item(pc, W(1, 1, 0, 1, TMP_WORD), "LDR R1,(TMP_WORD) ; ch_w")); pc++;
        items.add(new Item(pc, JZ(1, 0, 1, ADDR_MATCH_FOUND),
                           "JZ R1,@ADDR_MATCH_FOUND ; full match")); pc++;
        items.add(new Item(pc, JZ(0, 0, 1, ADDR_SEARCH_DONE),
                           "JZ R0,@ADDR_SEARCH_DONE ; paragraph ended")); pc++;
        items.add(new Item(pc, W(2, 0, 0, 0, TMP_CHAR), "STR R0,TMP_CHAR ; save ch_p")); pc++;
        items.add(new Item(pc, W(2, 1, 0, 0, TMP_R1),   "STR R1,TMP_R1 ; save ch_w")); pc++;
        items.add(new Item(pc, W(1, 2, 0, 0, TMP_CHAR), "LDR R2,TMP_CHAR ; R2 = ch_p")); pc++;
        items.add(new Item(pc, SMRW(2, 0, 0, TMP_R1),   "SMR R2,TMP_R1 ; R2 = ch_p - ch_w")); pc++;
        items.add(new Item(pc, JNE(2, 0, 1, ADDR_MISMATCH),
                           "JNE R2,@ADDR_MISMATCH ; chars differ")); pc++;
        items.add(new Item(pc, W(1, 3, 0, 0, TMP_PARA), "LDR R3,TMP_PARA")); pc++;
        items.add(new Item(pc, AIR(3, 1), "AIR R3,1 ; TMP_PARA++")); pc++;
        items.add(new Item(pc, W(2, 3, 0, 0, TMP_PARA), "STR R3,TMP_PARA")); pc++;

        // TMP_WORD++
        items.add(new Item(pc, W(1, 3, 0, 0, TMP_WORD), "LDR R3,TMP_WORD")); pc++;
        items.add(new Item(pc, AIR(3, 1), "AIR R3,1 ; TMP_WORD++")); pc++;
        items.add(new Item(pc, W(2, 3, 0, 0, TMP_WORD), "STR R3,TMP_WORD")); pc++;

        // Loop back
        items.add(new Item(pc, JMA(0, 1, ADDR_MATCH_LOOP), "JMA @ADDR_MATCH_LOOP ; continue matching")); pc++;


        int mismatchPC = pc;

        items.add(new Item(pc, W(1, 1, 0, 0, PTR_PARA),
                           "MIS: LDR R1,PTR_PARA")); pc++;
        items.add(new Item(pc, AIR(1, 1), "MIS: AIR R1,1 ; PTR_PARA++")); pc++;
        items.add(new Item(pc, W(2, 1, 0, 0, PTR_PARA),
                           "MIS: STR R1,PTR_PARA")); pc++;
        items.add(new Item(pc, JMA(0, 1, ADDR_SEARCH_LOOP),
                           "MIS: JMA @ADDR_SEARCH_LOOP ; continue")); pc++;


        int spaceHandlerPC = pc;
        items.add(new Item(pc, W(1, 0, 0, 0, 6), "SPACE: LDR R0,ZERO")); pc++;
        items.add(new Item(pc, AIR(0, 1),        "SPACE: AIR R0,1 ; LAST_DELIM=1")); pc++;
        items.add(new Item(pc, W(2, 0, 0, 0, LAST_DELIM), "SPACE: STR R0,LAST_DELIM")); pc++;
        items.add(new Item(pc, W(1, 1, 0, 0, PTR_PARA), "SPACE: LDR R1,PTR_PARA")); pc++;
        items.add(new Item(pc, AIR(1, 1), "SPACE: AIR R1,1 ; PTR_PARA++")); pc++;
        items.add(new Item(pc, W(2, 1, 0, 0, PTR_PARA), "SPACE: STR R1,PTR_PARA")); pc++;
        items.add(new Item(pc, JMA(0, 1, ADDR_SEARCH_LOOP),
                           "SPACE: JMA @ADDR_SEARCH_LOOP")); pc++;
        int dotHandlerPC = pc;
        items.add(new Item(pc, W(1, 0, 0, 0, SENT_NO), "DOT: LDR R0,SENT_NO")); pc++;
        items.add(new Item(pc, AIR(0, 1), "DOT: AIR R0,1 ; SENT_NO++")); pc++;
        items.add(new Item(pc, W(2, 0, 0, 0, SENT_NO), "DOT: STR R0,SENT_NO")); pc++;
        items.add(new Item(pc, W(1, 0, 0, 0, 6), "DOT: LDR R0,ZERO")); pc++;
        items.add(new Item(pc, W(2, 0, 0, 0, WORD_NO), "DOT: STR R0,WORD_NO")); pc++;
        items.add(new Item(pc, AIR(0, 1), "DOT: AIR R0,1 ; LAST_DELIM=1")); pc++;
        items.add(new Item(pc, W(2, 0, 0, 0, LAST_DELIM), "DOT: STR R0,LAST_DELIM")); pc++;
        items.add(new Item(pc, W(1, 1, 0, 0, PTR_PARA), "DOT: LDR R1,PTR_PARA")); pc++;
        items.add(new Item(pc, AIR(1, 1), "DOT: AIR R1,1 ; PTR_PARA++")); pc++;
        items.add(new Item(pc, W(2, 1, 0, 0, PTR_PARA), "DOT: STR R1,PTR_PARA")); pc++;
        items.add(new Item(pc, JMA(0, 1, ADDR_SEARCH_LOOP),
                           "DOT: JMA @ADDR_SEARCH_LOOP")); pc++;

        int matchFoundPC = pc;
        items.add(new Item(pc, W(1, 0, 0, 0, 6), "MF: LDR R0,ZERO")); pc++;
        items.add(new Item(pc, AIR(0, 1), "MF: AIR R0,1 ; FOUND=1")); pc++;
        items.add(new Item(pc, W(2, 0, 0, 0, FOUND), "MF: STR R0,FOUND")); pc++;
        items.add(new Item(pc, W(1, 0, 0, 0, SENT_NO), "MF: LDR R0,SENT_NO")); pc++;
        items.add(new Item(pc, W(2, 0, 0, 0, MATCH_SENT_NO), "MF: STR R0,MATCH_SENT_NO")); pc++;
        items.add(new Item(pc, W(1, 0, 0, 0, WORD_NO), "MF: LDR R0,WORD_NO")); pc++;
        items.add(new Item(pc, W(2, 0, 0, 0, MATCH_WORD_NO), "MF: STR R0,MATCH_WORD_NO")); pc++;
        char[] prefix1 = "FOUND AT SENTENCE ".toCharArray();
        for (char ch : prefix1) {
            items.add(new Item(pc, W(1, 0, 0, 0, 6), "MF: LDR R0,ZERO")); pc++;
            items.add(new Item(pc, AIR(0, ch), "MF: AIR R0,'" + ch + "'")); pc++;
            items.add(new Item(pc, OUTW(0, 2), "MF: OUT R0,2")); pc++;
        }
        items.add(new Item(pc, W(1, 0, 0, 0, SENT_NO), "MF: LDR R0,SENT_NO")); pc++;
        items.add(new Item(pc, AIR(0, '0'), "MF: AIR R0,'0' ; to ASCII")); pc++;
        items.add(new Item(pc, OUTW(0, 2), "MF: OUT SENT_NO")); pc++;
        items.add(new Item(pc, W(1, 0, 0, 0, 6), "MF: LDR R0,ZERO")); pc++;
        items.add(new Item(pc, AIR(0, ' '), "MF: AIR R0,' '")); pc++;
        items.add(new Item(pc, OUTW(0, 2), "MF: OUT R0,2")); pc++;
        char[] prefix2 = "WORD ".toCharArray();
        for (char ch : prefix2) {
            items.add(new Item(pc, W(1, 0, 0, 0, 6), "MF: LDR R0,ZERO")); pc++;
            items.add(new Item(pc, AIR(0, ch), "MF: AIR R0,'" + ch + "'")); pc++;
            items.add(new Item(pc, OUTW(0, 2), "MF: OUT R0,2")); pc++;
        }

        items.add(new Item(pc, W(1, 0, 0, 0, WORD_NO), "MF: LDR R0,WORD_NO")); pc++;
        items.add(new Item(pc, AIR(0, '0'), "MF: AIR R0,'0' ; to ASCII")); pc++;
        items.add(new Item(pc, OUTW(0, 2), "MF: OUT WORD_NO")); pc++;
        items.add(new Item(pc, W(1, 0, 0, 0, 6), "MF: LDR R0,ZERO")); pc++;
        items.add(new Item(pc, AIR(0, '\n'), "MF: AIR R0,'\\n'")); pc++;
        items.add(new Item(pc, OUTW(0, 2), "MF: OUT newline")); pc++;

        // Halt after match
        items.add(new Item(pc, 0, "HLT ; MATCH_FOUND done")); pc++;

        int searchDonePC = pc;
        items.add(new Item(pc, 0, "HLT ; SEARCH_DONE (no match)")); pc++;

        items.add(new Item(ADDR_PRINT_LOOP,  printLoop,
                "ADDR_PRINT_LOOP = " + toOct(printLoop,3)));
        items.add(new Item(ADDR_DONE_PRINT,  donePrint,
                "ADDR_DONE_PRINT = " + toOct(donePrint,3)));
        items.add(new Item(ADDR_READ_LOOP,   readLoop,
                "ADDR_READ_LOOP = " + toOct(readLoop,3)));
        items.add(new Item(ADDR_DONE_READ,   doneRead,
                "ADDR_DONE_READ = " + toOct(doneRead,3)));
        items.add(new Item(ADDR_SEARCH_LOOP, searchLoop,
                "ADDR_SEARCH_LOOP = " + toOct(searchLoop,3)));
        items.add(new Item(ADDR_MATCH_LOOP,  matchLoopPC,
                "ADDR_MATCH_LOOP = " + toOct(matchLoopPC,3)));
        items.add(new Item(ADDR_MISMATCH,    mismatchPC,
                "ADDR_MISMATCH = " + toOct(mismatchPC,3)));
        items.add(new Item(ADDR_SPACE_HANDLER, spaceHandlerPC,
                "ADDR_SPACE_HANDLER = " + toOct(spaceHandlerPC,3)));
        items.add(new Item(ADDR_DOT_HANDLER, dotHandlerPC,
                "ADDR_DOT_HANDLER = " + toOct(dotHandlerPC,3)));
        items.add(new Item(ADDR_MATCH_FOUND, matchFoundPC,
                "ADDR_MATCH_FOUND = " + toOct(matchFoundPC,3)));
        items.add(new Item(ADDR_SEARCH_DONE, searchDonePC,
                "ADDR_SEARCH_DONE = " + toOct(searchDonePC,3)));

        items.sort(Comparator.comparingInt(it -> it.addr));
        for (Item it : items) {
            String line = toOct(it.addr, 3) + " " + toOct(it.word, 6) + "    # " + it.comment;
            System.out.println(line);
        }
    }

    static class Item {
        int addr, word;
        String comment;
        Item(int a, int w, String c) { addr = a; word = w; comment = c; }
    }
}
