; ==============================
; Program2.asm - Project III skeleton
; ==============================
; Data section: sentence pointers/buffers + word buffer + flags
; ==============================
LOC 512                 ; Data start (decimal 512)

Sentence1:   Data 0     ; Will be filled by TRAP 1
Sentence2:   Data 0
Sentence3:   Data 0
Sentence4:   Data 0
Sentence5:   Data 0
Sentence6:   Data 0

WordBuf:     Data 0     ; Buffer / pointer for input word (TRAP 2)

FoundFlag:   Data 0     ; 0 = not found, 1 = found
FoundSent:   Data 0     ; sentence number (1..6)
FoundWord:   Data 0     ; word number in that sentence

ConstZero:   Data 0
ConstOne:    Data 1

; ==============================
; Code section
; ==============================
LOC 600                 ; Code start

Start: LDA 0,0,Sentence1    ; R0 = &Sentence1
       TRAP 1               ; OS/TRAP loads sentence 1

       LDA 0,0,Sentence2
       TRAP 1

       LDA 0,0,Sentence3
       TRAP 1

       LDA 0,0,Sentence4
       TRAP 1

       LDA 0,0,Sentence5
       TRAP 1

       LDA 0,0,Sentence6
       TRAP 1

       ; Print all 6 sentences (same style you had before)
       LDA 1,0,Sentence1    ; R1 = &Sentence1
       OUT 1,1              ; device 1 prints sentence 1

       LDA 1,0,Sentence2
       OUT 1,1

       LDA 1,0,Sentence3
       OUT 1,1

       LDA 1,0,Sentence4
       OUT 1,1

       LDA 1,0,Sentence5
       OUT 1,1

       LDA 1,0,Sentence6
       OUT 1,1

       ; (Optional) you can define and print a "ENTER WORD:" prompt
       ; string later, using OUT and a pointer, just like sentences.

ReadWord: LDA 0,0,WordBuf   ; R0 = &WordBuf
          TRAP 2            ; TRAP 2 reads the search word into WordBuf

InitFlags: LDR 1,0,ConstZero ; R1 = 0
           STR 1,0,FoundFlag
           STR 1,0,FoundSent
           STR 1,0,FoundWord

           ; ===============================================
           ; TODO: Add search + result reporting logic here
           ; After this point, you’ll:
           ;   - Loop over Sentence1..Sentence6
           ;   - For each sentence, loop over words
           ;   - Compare each word to the input word in WordBuf
           ;   - If found:
           ;       * FoundFlag = 1
           ;       * FoundSent = sentence number
           ;       * FoundWord = word number
           ;       * print result using OUT/TRAP
           ;   - If not found after all sentences:
           ;       * print "WORD NOT FOUND"
           ;
           ; Keep every label on same line as its instruction.
           ; Example:
           ;   SentenceLoop: LDR 0,0,ConstZero
           ;   NextWord:    LDR 0,0,ConstZero
           ; ===============================================

EndProgram: HLT           ; Stop execution
