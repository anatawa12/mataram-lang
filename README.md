# IMPORTANT: This repository is Work In Progress but frozen in effect. This repository may not be to be updated for long time.

# Mataram Lang: A high-level JVM assembly language

## Motivations

I want to decompile completely some class. 
However, I think it's not possible, so I (make and) use disassembler,
but there are some problem for assembly language.
The problem is assembler needs very long source code, 
so I want high-level assembly language which have features shown below.

## Features of this language will support

- Allow expression like shown below
  
      load var(1, Lsome/Class;).someField
      // means
      // aload 1
      // getfield some/Class.someField Ltype/to/SomeField; // this Ltype/to/SomeField; is resolved class file of some/Class.
      load var(1, int) + 1
      // means
      // iload 1
      // iconst_1
      // iadd

- have macros in language.

      $StringBuilder = `java/lang/StringBuilder`
      macro@method_code appendString(type valueType) {
          invokevirtual $StringBuilder append `($valueType)$StringBuilder`
      }

      new $StringBuilder // new `java/lang/StringBuilder`
      invokespecial $StringBuilder `<init>` `()V` // invokespecial `java/lang/StringBuilder` `<init>` `()V`
      ldc "someString"
      appendString `java/lang/String` // invokevirtual `java/lang/StringBuilder` append `(Ljava/lang/String;)Ljava/lang/StringBuilder;`

## Release plan

- First release may not support conditional macros.
