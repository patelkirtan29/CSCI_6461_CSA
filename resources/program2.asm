# Program2.asm - Paragraph Search Program (CSCI 6461 ISA)
# Reads a paragraph from memory, prints it, asks for a word, searches for it, and prints result

LOC 100
# Paragraph data (ASCII codes for 6 sentences, terminated by 0)
Data 72   ; 'H'
Data 101  ; 'e'
Data 108  ; 'l'
Data 108  ; 'l'
Data 111  ; 'o'
Data 46   ; '.'
Data 32   ; ' '
Data 84   ; 'T'
Data 104  ; 'h'
Data 105  ; 'i'
Data 115  ; 's'
Data 32   ; ' '
Data 105  ; 'i'
Data 115  ; 's'
Data 32   ; ' '
Data 97   ; 'a'
Data 32   ; ' '
Data 115  ; 's'
Data 101  ; 'e'
Data 110  ; 'n'
Data 116  ; 't'
Data 101  ; 'e'
Data 110  ; 'n'
Data 99   ; 'c'
Data 101  ; 'e'
Data 46   ; '.'
Data 32   ; ' '
Data 84   ; 'T'
Data 104  ; 'h'
Data 101  ; 'e'
Data 32   ; ' '
Data 113  ; 'q'
Data 117  ; 'u'
Data 105  ; 'i'
Data 99   ; 'c'
Data 107  ; 'k'
Data 32   ; ' '
Data 98   ; 'b'
Data 114  ; 'r'
Data 111  ; 'o'
Data 119  ; 'w'
Data 110  ; 'n'
Data 32   ; ' '
Data 102  ; 'f'
Data 111  ; 'o'
Data 120  ; 'x'
Data 46   ; '.'
Data 32   ; ' '
Data 65   ; 'A'
Data 32   ; ' '
Data 115  ; 's'
Data 105  ; 'i'
Data 109  ; 'm'
Data 112  ; 'p'
Data 108  ; 'l'
Data 101  ; 'e'
Data 32   ; ' '
Data 115  ; 's'
Data 101  ; 'e'
Data 110  ; 'n'
Data 116  ; 't'
Data 101  ; 'e'
Data 110  ; 'n'
Data 99   ; 'c'
Data 101  ; 'e'
Data 46   ; '.'
Data 32   ; ' '
Data 84   ; 'T'
Data 104  ; 'h'
Data 101  ; 'e'
Data 32   ; ' '
Data 100  ; 'd'
Data 111  ; 'o'
Data 103  ; 'g'
Data 32   ; ' '
Data 106  ; 'j'
Data 117  ; 'u'
Data 109  ; 'm'
Data 112  ; 'p'
Data 115  ; 's'
Data 46   ; '.'
Data 32   ; ' '
Data 84   ; 'T'
Data 104  ; 'h'
Data 101  ; 'e'
Data 32   ; ' '
Data 99   ; 'c'
Data 97   ; 'a'
Data 116  ; 't'
Data 32   ; ' '
Data 115  ; 's'
Data 108  ; 'l'
Data 101  ; 'e'
Data 101  ; 'e'
Data 112  ; 'p'
Data 115  ; 's'
Data 46   ; '.'
Data 0    ; End of paragraph sentinel

# Search word buffer (16 chars max)
LOC 200
Data 0
Data 0
Data 0
Data 0
Data 0
Data 0
Data 0
Data 0
Data 0
Data 0
Data 0
Data 0
Data 0
Data 0
Data 0
Data 0

# Temp variables
LOC 220
Data 1    ; sentenceIndex
Data 0    ; wordIndex
Data 0    ; curChar
Data 0    ; matched

# Prompt and messages
LOC 230
Data 69   ; 'E'
Data 110  ; 'n'
Data 116  ; 't'
Data 101  ; 'e'
Data 114  ; 'r'
Data 32   ; ' '
Data 119  ; 'w'
Data 111  ; 'o'
Data 114  ; 'r'
Data 100  ; 'd'
Data 58   ; ':'
Data 0
LOC 250
Data 70   ; 'F'
Data 111  ; 'o'
Data 117  ; 'u'
Data 110  ; 'n'
Data 100  ; 'd'
Data 58   ; ':'
Data 32   ; ' '
Data 0
LOC 260
Data 87   ; 'W'
Data 111  ; 'o'
Data 114  ; 'r'
Data 100  ; 'd'
Data 32   ; ' '
Data 110  ; 'n'
Data 111  ; 'o'
Data 116  ; 't'
Data 32   ; ' '
Data 102  ; 'f'
Data 111  ; 'o'
Data 117  ; 'u'
Data 110  ; 'n'
Data 100  ; 'd'
Data 0

# CODE SECTION
LOC 300
; Print paragraph
LDR 0,0,100         ; R0 = paragraph pointer
PRINT_LOOP LDR 1,0,0 ; R1 = [R0]
JZ 1,PRINT_END      ; If char == 0, end
OUT 1,1             ; Print char to printer
AIR 0,1             ; R0++
JUMP PRINT_LOOP
PRINT_END
; Print prompt
LDR 2,0,230         ; R2 = prompt pointer
PROMPT_LOOP LDR 3,0,0 ; R3 = [R2]
JZ 3,PROMPT_END     ; If char == 0, end
OUT 3,1             ; Print char
AIR 2,1             ; R2++
JUMP PROMPT_LOOP
PROMPT_END
; Read search word
LDR 4,0,200         ; R4 = search word buffer pointer
READ_LOOP IN 5,0    ; Read char from keyboard
JZ 5,READ_END       ; If char == 0, end
SIR 5,10            ; Check for newline (ASCII 10)
JZ 5,READ_END       ; If newline, end
STR 5,0,0           ; Store char in buffer
AIR 4,1             ; R4++
JUMP READ_LOOP
READ_END STR 6,0,0  ; Store 0 terminator
; Search paragraph for word
LDR 0,0,100         ; R0 = paragraph pointer
LDR 7,0,220         ; R7 = sentenceIndex
LDR 6,0,221         ; R6 = wordIndex
SEARCH_LOOP LDR 1,0,0 ; R1 = [R0]
JZ 1,NOT_FOUND      ; If char == 0, end
SIR 1,46            ; '.'
JZ 1,NEXT_SENTENCE
SIR 1,33            ; '!'
JZ 1,NEXT_SENTENCE
SIR 1,63            ; '?'
JZ 1,NEXT_SENTENCE
SIR 1,32            ; ' '
JZ 1,NEXT_WORD
SIR 1,44            ; ','
JZ 1,NEXT_WORD
AIR 6,1             ; wordIndex++
LDR 4,0,200         ; R4 = search word buffer
LDR 5,0,0           ; R5 = paragraph word pointer
WORD_MATCH_LOOP LDR 2,0,0 ; R2 = search word char
LDR 3,0,0           ; R3 = paragraph char
JZ 2,WORD_MATCH_END ; If search char == 0, end
JZ 3,WORD_MATCH_FAIL; If paragraph char == 0, fail
SIR 2,3             ; Compare chars
JNZ 2,WORD_MATCH_FAIL
AIR 4,1             ; Next search char
AIR 5,1             ; Next paragraph char
JUMP WORD_MATCH_LOOP
WORD_MATCH_END LDR 3,0,0 ; Next paragraph char
SIR 3,32            ; ' '
JZ 3,FOUND
SIR 3,44            ; ','
JZ 3,FOUND
SIR 3,46            ; '.'
JZ 3,FOUND
SIR 3,33            ; '!'
JZ 3,FOUND
SIR 3,63            ; '?'
JZ 3,FOUND
JUMP WORD_MATCH_FAIL
FOUND LDR 2,0,250    ; Found message
FOUND_MSG_LOOP LDR 3,0,0 ; R3 = [R2]
JZ 3,FOUND_MSG_END   ; If char == 0, end
OUT 3,1              ; Print char
AIR 2,1              ; R2++
JUMP FOUND_MSG_LOOP
FOUND_MSG_END LDR 4,0,200 ; Print word
PRINT_WORD_LOOP LDR 3,0,0 ; R3 = [R4]
JZ 3,PRINT_WORD_END  ; If char == 0, end
OUT 3,1              ; Print char
AIR 4,1              ; R4++
JUMP PRINT_WORD_LOOP
PRINT_WORD_END LDR 3,0,220 ; sentenceIndex
OUT 3,1
LDR 3,0,221         ; wordIndex
OUT 3,1
HLT
WORD_MATCH_FAIL JUMP NEXT_WORD
NEXT_WORD AIR 0,1    ; R0++
JUMP SEARCH_LOOP
NEXT_SENTENCE AIR 7,1 ; sentenceIndex++
LDA 6,0,0            ; wordIndex = 0
AIR 0,1              ; R0++
JUMP SEARCH_LOOP
NOT_FOUND LDR 2,0,260 ; Not found message
NOTFOUND_MSG_LOOP LDR 3,0,0 ; R3 = [R2]
JZ 3,NOTFOUND_MSG_END; If char == 0, end
OUT 3,1              ; Print char
AIR 2,1              ; R2++
JUMP NOTFOUND_MSG_LOOP
NOTFOUND_MSG_END HLT