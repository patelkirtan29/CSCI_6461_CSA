# Program2.asm - Paragraph Search Program (CSCI 6461 ISA)
# Reads a paragraph from memory, prints it, asks for a word, searches for it, and prints result

LOC 144
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
LOC 100
; Print paragraph
LDR 0,0,144         ; R0 = paragraph pointer
LDR 1,0,0           ; R1 = [R0]
JZ 1,312            ; If char == 0, end (PRINT_END)
OUT 1,1             ; Print char to printer
AIR 0,1             ; R0++
JZ 0,301            ; Unconditional jump to PRINT_LOOP
LDR 2,0,230         ; R2 = prompt pointer
LDR 3,0,0           ; R3 = [R2]
JZ 3,318            ; If char == 0, end (PROMPT_END)
OUT 3,1             ; Print char
AIR 2,1             ; R2++
JZ 0,314            ; Unconditional jump to PROMPT_LOOP
LDR 4,0,200         ; R4 = search word buffer pointer
	IN 0,0              ; Read char from keyboard (use R0)
JZ 5,325            ; If char == 0, end (READ_END)
SIR 5,10            ; Check for newline (ASCII 10)
JZ 5,325            ; If newline, end (READ_END)
	STR 0,0,0           ; Store char in buffer
AIR 4,1             ; R4++
JZ 0,320            ; Unconditional jump to READ_LOOP
	STR 1,0,0           ; Store 0 terminator (READ_END)
LDR 0,0,144         ; R0 = paragraph pointer
LDR 7,0,220         ; R7 = sentenceIndex
LDR 6,0,221         ; R6 = wordIndex
LDR 1,0,0           ; R1 = [R0]
JZ 1,340            ; If char == 0, end (NOT_FOUND)
SIR 1,46            ; '.'
JZ 1,335            ; NEXT_SENTENCE
SIR 1,33            ; '!'
JZ 1,335            ; NEXT_SENTENCE
SIR 1,63            ; '?'
JZ 1,335            ; NEXT_SENTENCE
SIR 1,32            ; ' '
JZ 1,332            ; NEXT_WORD
SIR 1,44            ; ','
JZ 1,332            ; NEXT_WORD
AIR 6,1             ; wordIndex++
LDR 4,0,200         ; R4 = search word buffer
LDR 5,0,0           ; R5 = paragraph word pointer
LDR 2,0,0           ; R2 = search word char
LDR 3,0,0           ; R3 = paragraph char
JZ 2,352            ; If search char == 0, end (WORD_MATCH_END)
JZ 3,357            ; If paragraph char == 0, fail (WORD_MATCH_FAIL)
	SIR 2,3             ; Compare chars
	JZ 2,347            ; If equal, continue WORD_MATCH_LOOP
	JZ 0,357            ; If not equal, unconditional jump to WORD_MATCH_FAIL
AIR 4,1             ; Next search char
AIR 5,1             ; Next paragraph char
JZ 0,347            ; Unconditional jump to WORD_MATCH_LOOP
LDR 3,0,0           ; Next paragraph char (WORD_MATCH_END)
SIR 3,32            ; ' '
JZ 3,359            ; FOUND
SIR 3,44            ; ','
JZ 3,359            ; FOUND
SIR 3,46            ; '.'
JZ 3,359            ; FOUND
SIR 3,33            ; '!'
JZ 3,359            ; FOUND
SIR 3,63            ; '?'
JZ 3,359            ; FOUND
JZ 0,357            ; Unconditional jump to WORD_MATCH_FAIL
LDR 2,0,250         ; Found message (FOUND)
LDR 3,0,0           ; R3 = [R2]
JZ 3,364            ; If char == 0, end (FOUND_MSG_END)
OUT 3,1             ; Print char
AIR 2,1             ; R2++
JZ 0,361            ; Unconditional jump to FOUND_MSG_LOOP
LDR 4,0,200         ; Print word (FOUND_MSG_END)
LDR 3,0,0           ; R3 = [R4]
JZ 3,368            ; If char == 0, end (PRINT_WORD_END)
OUT 3,1             ; Print char
AIR 4,1             ; R4++
JZ 0,366            ; Unconditional jump to PRINT_WORD_LOOP
LDR 3,0,220         ; sentenceIndex (PRINT_WORD_END)
OUT 3,1
LDR 3,0,221         ; wordIndex
OUT 3,1
HLT
JZ 0,332            ; Unconditional jump to WORD_MATCH_FAIL (address 357)
AIR 0,1             ; R0++ (NEXT_WORD)
JZ 0,326            ; Unconditional jump to SEARCH_LOOP (address 326)
AIR 7,1             ; sentenceIndex++ (NEXT_SENTENCE)
LDA 6,0,0           ; wordIndex = 0
AIR 0,1             ; R0++
JZ 0,326            ; Unconditional jump to SEARCH_LOOP (address 326)
LDR 2,0,260         ; Not found message (NOT_FOUND)
LDR 3,0,0           ; R3 = [R2]
JZ 3,377            ; If char == 0, end (NOTFOUND_MSG_END)
OUT 3,1             ; Print char
AIR 2,1             ; R2++
JZ 0,375            ; Unconditional jump to NOTFOUND_MSG_LOOP
HLT                 ; NOTFOUND_MSG_END