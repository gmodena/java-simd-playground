# java-simd-playground
Playing around with Java's Vector APIs

## Bytecode
Documents naive loop vs vectorized bytecode. Generate with godbolt, annotated
with the help of chatgpt.
### Array by scalar multiplication
```
 void mul(float[], float);
       0: iconst_0                        // Push int constant 0 onto the stack
       1: istore_3                        // Store the top of stack (0) into local variable 3 (i)
       2: iload_3                         // Load local variable 3 (i) onto the stack
       3: aload_1                         // Load local variable 1 (array) onto the stack
       4: arraylength                     // Get the length of the array on top of the stack
       5: if_icmpge     21                // If i >= array.length, jump to instruction at 21 (end of loop)
       8: aload_1                         // Load local variable 1 (array) onto the stack
       9: iload_3                         // Load local variable 3 (i) onto the stack
      10: dup2                            // Duplicate the top two stack values (array, i)
      11: faload                          // Load float from array[i]
      12: fload_2                         // Load local variable 2 (multiplier) onto the stack
      13: fmul                            // Multiply the two floats on the stack (array[i] * multiplier)
      14: fastore                         // Store the result back into array[i]
      15: iinc          3, 1              // Increment local variable 3 (i) by 1
      18: goto          2                 // Go back to the beginning of the loop (instruction at 2)
      21: return                          // Return from the method
```
```
void vectorizedMul(float[], float);
       0: iconst_0                          // Push int constant 0 onto the stack (loop index initialization)
       1: istore_3                          // Store the loop index (0) in local variable 3
       2: getstatic     #7                  // Get the VectorSpecies representing preferred vector length
       5: astore        4                   // Store the VectorSpecies in local variable 4
       7: aload         4                   // Load the VectorSpecies onto the stack
       9: aload_1                           // Load the float array onto the stack
      10: arraylength                       // Get the length of the float array
      11: invokeinterface #37,  2           // Invoke the loopBound method of VectorSpecies with the array length
      16: istore        5                   // Store the loop bound in local variable 5
      18: aload         4                   // Load the VectorSpecies onto the stack
      20: fload_2                           // Load the float constant onto the stack
      21: invokestatic  #41                 // Invoke the broadcast method of FloatVector to broadcast the float value
      24: astore        6                   // Store the broadcasted float value in local variable 6
      26: iload_3                           // Load the loop index onto the stack
      27: iload         5                   // Load the loop bound onto the stack
      29: if_icmpge     70                  // If the loop index >= loop bound, jump to 70 (end of loop)
      32: aload         4                   // Load the VectorSpecies onto the stack
      34: aload_1                           // Load the float array onto the stack
      35: iload_3                           // Load the loop index onto the stack
      36: invokestatic  #19                 // Invoke the fromArray method of FloatVector to create a FloatVector
      39: astore        7                   // Store the FloatVector in local variable 7
      41: aload         7                   // Load the FloatVector onto the stack
      43: aload         6                   // Load the broadcasted float value onto the stack
      45: invokevirtual #23                 // Invoke the mul method of FloatVector to perform element-wise multiplication
      48: astore        8                   // Store the result of multiplication in local variable 8
      50: aload         8                   // Load the result FloatVector onto the stack
      52: aload_1                           // Load the float array onto the stack
      53: iload_3                           // Load the loop index onto the stack
      54: invokevirtual #45                 // Invoke the intoArray method of FloatVector to store the result in the array
      57: iload_3                           // Load the loop index onto the stack
      58: aload         4                   // Load the VectorSpecies onto the stack
      60: invokeinterface #13,  1           // Invoke the length method of VectorSpecies
      65: iadd                              // Add the length of the vector to the loop index
      66: istore_3                          // Store the new loop index
      67: goto          26                  // Go back to the beginning of the loop
      70: iload_3                           // Load the loop index onto the stack
      71: aload_1                           // Load the float array onto the stack
      72: arraylength                       // Get the length of the float array
      73: if_icmpge     89                  // If the loop index >= array length, jump to 89 (end of loop)
      76: aload_1                           // Load the float array onto the stack
      77: iload_3                           // Load the loop index onto the stack
      78: dup2                              // Duplicate the top two stack values (index and array reference)
      79: faload                            // Load the float value from the array at the given index
      80: fload_2                           // Load the float constant onto the stack
      81: fmul                              // Multiply the float value with the constant
      82: fastore                           // Store the result back into the array at the same index
      83: iinc          3, 1                // Increment the loop index
      86: goto          70                  // Go back to the beginning of the loop
      89: return                            // Return from the method
```
### Dot product
```
 void dot(float[], float[]);
       0: iconst_0                         // Push int constant 0 onto the stack
       1: istore_3                         // Store the top of stack (0) into local variable 3 (result)
       2: iconst_0                         // Push int constant 0 onto the stack
       3: istore        4                  // Store the top of stack (0) into local variable 4 (i)
       5: iload         4                  // Load local variable 4 (i) onto the stack
       7: aload_1                          // Load local variable 1 (array1) onto the stack
       8: arraylength                      // Get the length of the array on top of the stack
       9: if_icmpge     32                 // If i >= array1.length, jump to instruction at 32 (end of loop)
      12: iload_3                          // Load local variable 3 (result) onto the stack
      13: i2f                              // Convert int result to float
      14: aload_1                          // Load local variable 1 (array1) onto the stack
      15: iload         4                  // Load local variable 4 (i) onto the stack
      17: faload                           // Load float from array1[i]
      18: aload_2                          // Load local variable 2 (array2) onto the stack
      19: iload         4                  // Load local variable 4 (i) onto the stack
      21: faload                           // Load float from array2[i]
      22: fmul                             // Multiply the two floats on the stack (array1[i] * array2[i])
      23: fadd                             // Add the result to the current float result
      24: f2i                              // Convert the float result back to int
      25: istore_3                         // Store the int result back into local variable 3
      26: iinc          4, 1               // Increment local variable 4 (i) by 1
      29: goto          5                  // Go back to the beginning of the loop (instruction at 5)
      32: return                           // Return from the method
```
```
void vectorizedDot(float[], float[]);
       0: getstatic     #7                  // Get the VectorSpecies representing preferred vector length
       3: astore_3                          // Store the VectorSpecies in local variable 3
       4: iconst_0                          // Push int constant 0 onto the stack (loop index initialization)
       5: istore        4                   // Store the loop index (0) in local variable 4
       7: fconst_0                          // Push float constant 0 onto the stack (result initialization)
       8: fstore        5                   // Store the result (0.0f) in local variable 5
      10: iload         4                   // Load the loop index onto the stack
      12: aload_1                           // Load the first float array onto the stack
      13: arraylength                       // Get the length of the first array
      14: aload_3                           // Load the VectorSpecies onto the stack
      15: invokeinterface #13,  1           // Invoke the length method of VectorSpecies
      20: isub                              // Subtract the length of the vector from the length of the array
      21: if_icmpge     74                  // If the loop index >= (array length - vector length), jump to 74 (end of loop)
      24: aload_3                           // Load the VectorSpecies onto the stack
      25: aload_1                           // Load the first float array onto the stack
      26: iload         4                   // Load the loop index onto the stack
      28: invokestatic  #19                 // Create a FloatVector from the array and index
      31: astore        6                   // Store the FloatVector in local variable 6
      33: aload_3                           // Load the VectorSpecies onto the stack
      34: aload_2                           // Load the second float array onto the stack
      35: iload         4                   // Load the loop index onto the stack
      37: invokestatic  #19                 // Create a FloatVector from the array and index
      40: astore        7                   // Store the FloatVector in local variable 7
      42: fload         5                   // Load the result onto the stack
      44: aload         6                   // Load the first FloatVector onto the stack
      46: aload         7                   // Load the second FloatVector onto the stack
      48: invokevirtual #23                 // Multiply and reduce the elements of the vectors
      51: getstatic     #27                 // Get the VectorOperator representing addition
      54: invokevirtual #33                 // Reduce the lanes and perform addition
      57: fadd                              // Add the result to the current float result
      58: fstore        5                   // Store the new result in local variable 5
      60: iload         4                   // Load the loop index onto the stack
      62: aload_3                           // Load the VectorSpecies onto the stack
      63: invokeinterface #13,  1           // Invoke the length method of VectorSpecies
      68: iadd                              // Add the length of the vector to the loop index
      69: istore        4                   // Store the new loop index
      71: goto          10                  // Go back to the beginning of the loop
      74: iload         4                   // Load the loop index onto the stack
      76: aload_1                           // Load the first float array onto the stack
      77: arraylength                       // Get the length of the first array
      78: if_icmpge     101                 // If the loop index >= the array length, jump to 101 (end of loop)
      81: fload         5                   // Load the result onto the stack
      83: aload_1                           // Load the first float array onto the stack
      84: iload         4                   // Load the loop index onto the stack
      86: faload                            // Load the float value from the first array
      87: aload_2                           // Load the second float array onto the stack
      88: iload         4                   // Load the loop index onto the stack
      90: faload                            // Load the float value from the second array
      91: fmul                              // Multiply the float values
      92: fadd                              // Add the result to the current float result
      93: fstore        5                   // Store the new result in local variable 5
      95: iinc          4, 1                // Increment the loop index
      98: goto          74                  // Go back to the beginning of the loop
     101: return                            // Return from the method
```

# Benchmarks
```
$ cat /proc/cpuinfo | grep "model name" | head -n 1
model name      : 12th Gen Intel(R) Core(TM) i5-1240P
```

Run jhm benchmarks
```
$ ./gradlew clean run
Benchmark                  Mode  Cnt  Score   Error  Units
Runner.benchDot            avgt    5  0.012 ± 0.001   s/op
Runner.benchMul            avgt    5  0.009 ± 0.002   s/op
Runner.benchVectorizedDot  avgt    5  0.012 ± 0.002   s/op
Runner.benchVectorizedMul  avgt    5  0.008 ± 0.001   s/op
```
