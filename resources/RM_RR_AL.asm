# ARITHMETIC OPERATIONS (Register-Memory & Register-Register)
LOC 200
Data 15
Data 25
Data 0                   ; Result storage
Data 10
Data 30
Data 8
Data 16
Data 25
Data 13
Data 9
Data 6
Data 7

; Memory-based arithmetic
LDR 1,0,200             ; Load R1 = 15
AMR 1,0,201             ; Add memory[201] to R1 (R1 = 15 + 25 = 40)
STR 1,0,202             ; Store result
LDR 2,0,200             ; Load R2 = 15  
SMR 2,0,201             ; Subtract memory[201] from R2 (R2 = 15 - 25 = -10)

; Immediate arithmetic
LDR 1,0,200             ; Load R1 = 15
AIR 1,10                ; Add immediate 10 to R1 (R1 = 25)
SIR 1,5                 ; Subtract immediate 5 from R1 (R1 = 20)