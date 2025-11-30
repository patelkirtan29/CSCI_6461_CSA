package com.gwu.simulator;

import java.util.*;

public class GenProgram2 {
    static int W(int op,int r,int ix,int i,int addr){
        return (op<<10) | (r<<8) | (ix<<6) | (i<<5) | (addr & 0x1F);
    }
    static int AIR(int r,int imm){
        return (6<<10) | (r<<8) | (imm & 0xFF);
    }
    static int SIR(int r,int imm){
        return (7<<10) | (r<<8) | (imm & 0xFF);
    }
    static int INW(int r,int dev){
        return (61<<10) | (r<<6) | (dev & 0x1F);
    }
    static int OUTW(int r,int dev){
        return (62<<10) | (r<<6) | (dev & 0x1F);
    }
    static int SOBW(int r,int ix,int i,int addr){
        return (16<<10) | (r<<8) | (ix<<6) | (i<<5) | (addr & 0x1F);
    }
    static int TRR(int r1, int r2){
        return (22<<10) | (r1<<8) | (r2<<6);
    }
    static int JZ(int r,int ix,int i,int addr){
        return (10<<10) | (r<<8) | (ix<<6) | (i<<5) | (addr & 0x1F);
    }
    static int JNE(int r,int ix,int i,int addr){
        return (11<<10) | (r<<8) | (ix<<6) | (i<<5) | (addr & 0x1F);
    }
    static int JMA(int ix,int i,int addr){
        return (13<<10) | (ix<<6) | (i<<5) | (addr & 0x1F);
    }
    static String toOct(int v, int width){
        String s = Integer.toOctalString(v & 0xFFFF);
        while (s.length() < width) s = "0"+s;
        return s;
    }
    public static void main(String[] args){
        List<Item> items = new ArrayList<>();
        
        // Data region
        items.add(new Item(1, 6, "NUM_SENT = 6"));
        items.add(new Item(2, 0x100, "META_BASE = 0o400"));
        items.add(new Item(3, 0x200, "SENT_BASE = 0o1000"));
        items.add(new Item(4, 0, "SENT_IDX = 0"));
        items.add(new Item(5, 0, "WORD_IDX = 0"));
        items.add(new Item(6, 0, "FOUND_SENT = 0"));
        items.add(new Item(7, 0, "FOUND_POS = 0"));
        items.add(new Item(10, 0, "SEARCH_WORD"));
        items.add(new Item(11, 0, "CURR_SENT_ADDR"));
        items.add(new Item(12, 0, "CURR_CHAR"));
        
        // Address placeholders for indirect jumps
        int ADDR_READ_WORD = 15;
        int ADDR_SENT_LOOP = 16;
        int ADDR_WORD_LOOP = 17;
        int ADDR_NOT_FOUND = 20;
        
        int a = 64; // code base 0o100
        
        // ===== Read search word (character by character) =====
        items.add(new Item(a, INW(0, 0), "IN R0,0 ; read word char")); a++;
        items.add(new Item(a, W(2,0,0,0,10), "STR R0,10 ; store char")); a++;
        items.add(new Item(a, JZ(0,0,1,ADDR_SENT_LOOP), "JZ R0,@ADDR_SENT_LOOP ; if null, done")); a++;
        items.add(new Item(a, JMA(0,1,ADDR_READ_WORD), "JMA @ADDR_READ_WORD ; loop")); a++;
        
        int sentLoop = a;
        items.add(new Item(a, W(1,1,0,0,1), "LDR R1,1 ; NUM_SENT")); a++;
        items.add(new Item(a, W(1,2,0,0,2), "LDR R2,2 ; META_BASE")); a++;
        items.add(new Item(a, W(2,1,0,0,4), "STR R1,4 ; init SENT_IDX")); a++;
        
        // int loopSent = a; // sentence loop label
        items.add(new Item(a, W(1,1,0,0,4), "LDR R1,4 ; SENT_IDX")); a++;
        items.add(new Item(a, SIR(1, 1), "SIR R1,1 ; decrement")); a++;
        items.add(new Item(a, W(2,1,0,0,4), "STR R1,4 ; save")); a++;
        items.add(new Item(a, JZ(1,0,1,ADDR_NOT_FOUND), "JZ R1,@ADDR_NOT_FOUND ; if all done, not found")); a++;
        
        // Load sentence start address from metadata
        items.add(new Item(a, W(1,1,0,0,4), "LDR R1,4 ; SENT_IDX")); a++;
        items.add(new Item(a, W(1,2,0,0,2), "LDR R2,2 ; META_BASE")); a++;
        items.add(new Item(a, AIR(2, 0), "AIR R2,0 ; add offset (simplified)")); a++;
        items.add(new Item(a, W(1,2,0,0,2), "LDR R2,(2) ; load sent addr")); a++;
        items.add(new Item(a, W(2,2,0,0,11), "STR R2,11 ; CURR_SENT_ADDR")); a++;
        
        // Reset word index for new sentence
        items.add(new Item(a, W(2,0,0,0,5), "STR R0,5 ; WORD_IDX = 0")); a++;
        
        int loopWord = a; // word loop label
        items.add(new Item(a, W(1,2,0,0,11), "LDR R2,11 ; CURR_SENT_ADDR")); a++;
        items.add(new Item(a, W(1,2,0,1,2), "LDR R1,(R2) ; load char")); a++;
        items.add(new Item(a, W(2,1,0,0,12), "STR R1,12 ; CURR_CHAR")); a++;
        items.add(new Item(a, JZ(1,0,1,ADDR_SENT_LOOP), "JZ R1,@ADDR_SENT_LOOP ; if null, next sent")); a++;
        
        // Compare current char with search word char
        items.add(new Item(a, W(1,1,0,0,12), "LDR R1,12 ; CURR_CHAR")); a++;
        items.add(new Item(a, W(1,3,0,0,10), "LDR R3,10 ; SEARCH_WORD")); a++;
        items.add(new Item(a, TRR(1,3), "TRR R1,R3 ; compare")); a++;
        items.add(new Item(a, JNE(0,0,1,ADDR_WORD_LOOP), "JNE R0,@ADDR_WORD_LOOP ; if not equal, next")); a++;
        
        // Character matched! Output results
        items.add(new Item(a, W(1,0,0,0,10), "LDR R0,10 ; search word")); a++;
        items.add(new Item(a, OUTW(0,1), "OUT R0,1 ; print word")); a++;
        items.add(new Item(a, W(1,0,0,0,4), "LDR R0,4 ; SENT_IDX")); a++;
        items.add(new Item(a, OUTW(0,1), "OUT R0,1 ; print sentence")); a++;
        items.add(new Item(a, W(1,0,0,0,5), "LDR R0,5 ; WORD_IDX")); a++;
        items.add(new Item(a, OUTW(0,1), "OUT R0,1 ; print position")); a++;
        items.add(new Item(a, W(0,0,0,0,0), "HLT")); a++;
        
        // Not found
        int notFound = a;
        items.add(new Item(a, W(0,0,0,0,0), "HLT ; word not found")); a++;
        
        // Fill address constants
        items.add(new Item(ADDR_READ_WORD, a - (a - 64), "ADDR_READ_WORD"));
        items.add(new Item(ADDR_SENT_LOOP, sentLoop, "ADDR_SENT_LOOP = " + toOct(sentLoop, 3)));
        items.add(new Item(ADDR_WORD_LOOP, loopWord, "ADDR_WORD_LOOP = " + toOct(loopWord, 3)));
        items.add(new Item(ADDR_NOT_FOUND, notFound, "ADDR_NOT_FOUND = " + toOct(notFound, 3)));
        
        items.sort(Comparator.comparingInt(it -> it.addr));
        for (Item it : items){
            String line = toOct(it.addr, 3) + " " + toOct(it.word, 6) + "    # " + it.comment;
            System.out.println(line);
        }
    }
    static class Item{
        int addr, word; String comment;
        Item(int a,int w,String c){ addr=a; word=w; comment=c; }
    }
}
