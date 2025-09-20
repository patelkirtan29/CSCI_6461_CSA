START:  LDA 0,0,VALUE      ; Load VALUE into R0
        SRC 0,3,0,0        ; Shift R0 left by 3 (logical)
        RRC 0,2,1,1        ; Rotate R0 right by 2 (circular)
        HLT                ; Stop execution
LOC 20
VALUE: DATA 25             ; Initial value = 25