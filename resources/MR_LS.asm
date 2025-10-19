#LOAD/STORE OPERATIONS (Memory-Register Transfer)

LOC 10
Data 10 ; memory[10] = 10
Data 100 ; memory[11] = 100
Data 255 ; memory[12] = 255
Data 512 ; memory[13] = 512
Data 1024 ; memory[14] = 1024
Data 0 ; memory[15] = 0

LDR 1,0,10 ; Load R1 from memory[10] -> R1 = 10
STR 1,0,16 ; Store R1 to memory[16]
LDR 2,0,11 ; Load R2 from memory[11] -> R2 = 100
STR 2,0,17 ; Store R2 to memory[17]

LDA 3,0,20 ; Load R3 with effective address 20
STR 3,0,18 ; Store R3 value to memory[18]

LDX 1,12 ; Load X1 from memory[12] = 255
STX 1,0,19 ; Store X1 to memory[19]
LDX 2,13 ; Load X2 from memory[13] = 512
STX 2,0,20 ; Store X2 to memory[20]
LDX 3,14 ; Load X3 from memory[14] = 1024
STX 3,0,21 ; Store X3 to memory[21]

LDR 0,1,10 ; Load R0 using X1 as index -> EA = 10 + X1
STR 0,1,15 ; Store R0 into memory[15 + X1]

HLT ; Stop execution
