#LOAD/STORE OPERATIONS (Memory-Register Transfer)

LOC 100
Data 10 ; Test data
Data 100
Data 255 

; Load/Store Register operations
LDR 1,0,100 ; Load R1 from memory[100] (value 42)
STR 1,0,101 ; Store R1 to memory[101]

; Load Address (immediate)
LDA 2,0,500 ; Load R2 with immediate value 500

; Index register operations  
LDX 1,200   ; Load X1 with immediate value 200
STX 1,0,102 ; Store X1 to memory[102]