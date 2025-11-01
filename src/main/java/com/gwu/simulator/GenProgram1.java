package com.gwu.simulator;

import java.util.*;

public class GenProgram1 {
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
    static int AMRW(int r,int ix,int i,int addr){
        return (4<<10) | (r<<8) | (ix<<6) | (i<<5) | (addr & 0x1F);
    }
    static int SMRW(int r,int ix,int i,int addr){
        return (5<<10) | (r<<8) | (ix<<6) | (i<<5) | (addr & 0x1F);
    }
    static int NOTW(int r){
        return (25<<10) | (r<<8);
    }
    static int JCC(int ccBit,int ix,int i,int addr){
        return (12<<10) | (ccBit<<8) | (ix<<6) | (i<<5) | (addr & 0x1F);
    }
    static String toOct(int v, int width){
        String s = Integer.toOctalString(v & 0xFFFF);
        while (s.length() < width) s = "0"+s;
        return s;
    }
    public static void main(String[] args){
        List<Item> items = new ArrayList<>();
    // Data region
    items.add(new Item(1, 20, "N = 20"));
        items.add(new Item(2, 512, "BASE = 0o1000"));
        items.add(new Item(3, 64, "CODE = 0o100"));
        items.add(new Item(5, 512, "PTR = 0o1000"));
        items.add(new Item(6, 0x7FFF, "BESTDIFF = 077777"));
        items.add(new Item(7, 0, "BESTVAL = 0"));
        items.add(new Item(10, 0, "SEARCH TMP"));
        items.add(new Item(11, 0, "DIFF TMP"));
        // Placeholders for code target addresses (filled after code layout)
        int ADDR_LOOP_IN = 12;
        int ADDR_LOOP2 = 13;
    int ADDR_ABS_FIX = 14;
    int ADDR_SKIP_UPD = 15;
    int ADDR_LOOP_PRINT = 16;

        int a = 64; // code base 0o100
        // Init index regs and load count
        items.add(new Item(a, W(33,0,1,0,2), "LDX X1,2")); a++;
        items.add(new Item(a, W(33,0,2,0,3), "LDX X2,3")); a++;
        items.add(new Item(a, W(1,1,0,0,1), "LDR R1,1")); a++;
        int loopIn = a; // input loop label
        items.add(new Item(a, INW(0,0), "IN R0,0")); a++;
        items.add(new Item(a, W(2,0,0,1,5), "STR R0,(5)")); a++;
        items.add(new Item(a, W(1,2,0,0,5), "LDR R2,5")); a++;
        items.add(new Item(a, AIR(2,1), "AIR R2,1")); a++;
        items.add(new Item(a, W(2,2,0,0,5), "STR R2,5")); a++;
    // SOB using indirect to ADDR_LOOP_IN
        items.add(new Item(a, SOBW(1,0,1, ADDR_LOOP_IN), "SOB R1,@ADDR_LOOP_IN")); a++;
    // After input: print the N numbers
    items.add(new Item(a, W(1,1,0,0,1), "LDR R1,1 ; reload count")); a++;
    items.add(new Item(a, W(1,2,0,0,2), "LDR R2,2 ; BASE")); a++;
    items.add(new Item(a, W(2,2,0,0,5), "STR R2,5 ; reset PTR")); a++;
    int loopPrint = a; // print loop label
    items.add(new Item(a, W(1,0,0,1,5), "LDR R0,(5)")); a++;
    items.add(new Item(a, OUTW(0,1), "OUT R0,1 ; print value")); a++;
    items.add(new Item(a, W(1,2,0,0,5), "LDR R2,5")); a++;
    items.add(new Item(a, AIR(2,1), "AIR R2,1")); a++;
    items.add(new Item(a, W(2,2,0,0,5), "STR R2,5")); a++;
    items.add(new Item(a, SOBW(1,0,1, ADDR_LOOP_PRINT), "SOB R1,@ADDR_LOOP_PRINT")); a++;
    // Reset ptr and get search value
        items.add(new Item(a, W(1,2,0,0,2), "LDR R2,2")); a++;
        items.add(new Item(a, W(2,2,0,0,5), "STR R2,5")); a++;
        items.add(new Item(a, INW(3,0), "IN R3,0  ; search")); a++;
        items.add(new Item(a, W(2,3,0,0,10), "STR R3,10 ; save search")); a++;
        // Prepare for second loop: reload count and reset ptr
        items.add(new Item(a, W(1,1,0,0,1), "LDR R1,1")); a++;
        items.add(new Item(a, W(1,2,0,0,2), "LDR R2,2")); a++;
        items.add(new Item(a, W(2,2,0,0,5), "STR R2,5")); a++;
        int loop2 = a; // start of search/compare loop
        // Load current value and compute abs diff into R0
        items.add(new Item(a, W(1,0,0,1,5), "LDR R0,(5)")); a++;
        items.add(new Item(a, SMRW(0,0,0,10), "SMR R0,10  ; R0-=search")); a++;
        // If Negative (CC bit3), jump to ABS_FIX (indirect)
        items.add(new Item(a, JCC(3,0,1, ADDR_ABS_FIX), "JCC N,@ABS_FIX")); a++;
        // Non-negative falls through; store abs diff to temp at 11
        items.add(new Item(a, W(2,0,0,0,11), "STR R0,11 ; save diff")); a++;
        // Compare bestDiff - diff: R2 <- M[6]; R2 -= M[11]
        items.add(new Item(a, W(1,2,0,0,6), "LDR R2,6 ; bestDiff")); a++;
        items.add(new Item(a, SMRW(2,0,0,11), "SMR R2,11")); a++;
        // If Negative (bestDiff < diff), skip update (indirect)
        items.add(new Item(a, JCC(3,0,1, ADDR_SKIP_UPD), "JCC N,@SKIP_UPD")); a++;
        // Update bestDiff and bestVal
        items.add(new Item(a, W(1,2,0,0,11), "LDR R2,11")); a++;
        items.add(new Item(a, W(2,2,0,0,6), "STR R2,6 ; bestDiff=diff")); a++;
        items.add(new Item(a, W(1,2,0,1,5), "LDR R2,(5) ; current")); a++;
        items.add(new Item(a, W(2,2,0,0,7), "STR R2,7 ; bestVal=current")); a++;
        int skipUpd = a; // target for skip update
        // Increment ptr and continue loop
        items.add(new Item(a, W(1,2,0,0,5), "LDR R2,5")); a++;
        items.add(new Item(a, AIR(2,1), "AIR R2,1")); a++;
        items.add(new Item(a, W(2,2,0,0,5), "STR R2,5")); a++;
        // SOB R1 -> loop2 (indirect)
        items.add(new Item(a, SOBW(1,0,1, ADDR_LOOP2), "SOB R1,@ADDR_LOOP2")); a++;
        // After loop: print search and bestVal
        items.add(new Item(a, OUTW(3,1), "OUT R3,1 ; print search")); a++;
        items.add(new Item(a, W(1,0,0,0,7), "LDR R0,7")); a++;
        items.add(new Item(a, OUTW(0,1), "OUT R0,1 ; print best")); a++;
        items.add(new Item(a, W(0,0,0,0,0), "HLT")); a++;

        // ABS_FIX block (compute two's complement of R0 and store diff to 11, then branch back to compare path)
        int absFix = a;
        items.add(new Item(a, NOTW(0), "NOT R0")); a++;
        items.add(new Item(a, AIR(0,1), "AIR R0,1 ; two's comp")); a++;
        items.add(new Item(a, W(2,0,0,0,11), "STR R0,11 ; save diff")); a++;
        // Compare bestDiff - diff again
        items.add(new Item(a, W(1,2,0,0,6), "LDR R2,6 ; bestDiff")); a++;
        items.add(new Item(a, SMRW(2,0,0,11), "SMR R2,11")); a++;
        items.add(new Item(a, JCC(3,0,1, ADDR_SKIP_UPD), "JCC N,@SKIP_UPD")); a++;
        // Update if needed
        items.add(new Item(a, W(1,2,0,0,11), "LDR R2,11")); a++;
        items.add(new Item(a, W(2,2,0,0,6), "STR R2,6")); a++;
        items.add(new Item(a, W(1,2,0,1,5), "LDR R2,(5)")); a++;
        items.add(new Item(a, W(2,2,0,0,7), "STR R2,7")); a++;
        // Jump back to skipUpd (needed because skipUpd is at lower address than absFix)
        items.add(new Item(a, W(13,0,0,1,ADDR_SKIP_UPD), "JMA @ADDR_SKIP_UPD")); a++;

        // Fill the address constants now that labels are known
        items.add(new Item(ADDR_LOOP_IN, loopIn, "ADDR_LOOP_IN = "+toOct(loopIn,3)));
        items.add(new Item(ADDR_LOOP2, loop2, "ADDR_LOOP2 = "+toOct(loop2,3)));
        items.add(new Item(ADDR_ABS_FIX, absFix, "ADDR_ABS_FIX = "+toOct(absFix,3)));
    items.add(new Item(ADDR_SKIP_UPD, skipUpd, "ADDR_SKIP_UPD = "+toOct(skipUpd,3)));
    items.add(new Item(ADDR_LOOP_PRINT, loopPrint, "ADDR_LOOP_PRINT = "+toOct(loopPrint,3)));

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
