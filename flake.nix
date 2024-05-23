{
  description = "Flake to manage a Java 21 workspace.";

  inputs.nixpkgs.url = "nixpkgs/nixpkgs-unstable";

  outputs = inputs:
    let
      system = "x86_64-linux";
      pkgs = inputs.nixpkgs.legacyPackages.${system};
    in
    {
      devShell.${system} = pkgs.mkShell rec {
        name = "java-shell";
        buildInputs = with pkgs; [ jdk21 gradle ];

        shellHook = ''
          export JAVA_HOME=${pkgs.jdk21}
          PATH="${pkgs.jdk21}/bin:$PATH"
        '';
      };
    };
}
