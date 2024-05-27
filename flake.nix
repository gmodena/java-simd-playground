{
  description = "Flake to manage a Java 22 workspace.";

  inputs.nixpkgs.url = "nixpkgs/nixpkgs-unstable";

  outputs = inputs:
    let
      system = "x86_64-linux";
      overlay = import ./overlays/default.nix;
      pkgs = (inputs.nixpkgs.legacyPackages.${system}.extend overlay);
    in
    {
      devShell.${system} = pkgs.mkShell rec {
        name = "java-shell";
        buildInputs = with pkgs; [ jdk22 gradle llvm];

        shellHook = ''
          export JAVA_HOME=${pkgs.jdk22}
          PATH="${pkgs.jdk22}/bin:$PATH"
        '';
      };
    };
}
