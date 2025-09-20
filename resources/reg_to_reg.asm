LOC 6 ;BEGIN AT LOCATION 6
Data 10 ;PUT 10 AT LOCATION 6
Data 3 ;PUT 3 AT LOCATION 7
Data End ;PUT 1024 AT LOCATION
MLT 0,2 ; Multiply Register by Register
MLT 2,0 ; Multiply Register by Register
DVD 0,2 ; Divide Register by Register
DVD 2,0 ; Divide Register by Register
TRR 1,2 ; Test the Equality of Register and Register
AND 1,2 ; Logical And of Register and Register
ORR 1,2 ; Logical Or of Register and Register
NOT 1 ; Logical Not of Register To Register
LOC 1024
End: HLT ;STOP 