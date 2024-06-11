{
  description = "Flake to manage a Java 22 workspace.";

  inputs.nixpkgs.url = "nixpkgs/nixpkgs-unstable";
  inputs.hsdis-jdk22.url = "github:gmodena/hsdis-jdk22";

  outputs = inputs:
  let
      system = "x86_64-linux";
      pkgs = inputs.nixpkgs.legacyPackages.${system};
      hsdis-jdk = inputs.hsdis-jdk22.packages.${system}.default;
    in
    {
      devShell.${system} = pkgs.mkShell rec {
        name = "java-shell";
        buildInputs = [ hsdis-jdk pkgs.gradle pkgs.llvm];

        shellHook = ''
          export JAVA_HOME=${hsdis-jdk}
          PATH="${hsdis-jdk}/bin:$PATH"
        '';
      };
    };
}
