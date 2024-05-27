final: prev:
{
  jdk22 = prev.jdk22.overrideAttrs (old: {
    buildInputs = old.buildInputs ++ [ final."llvm" ];
    configureFlags = old.configureFlags ++ [
      "--with-hsdis=llvm"
      "--with-llvm=${final.llvm.dev}"
    ];
  });
}
