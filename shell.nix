{ sources ? import ./nix/sources.nix, pkgs ? import sources.nixpkgs { } }:
with pkgs;
mkShell {
  buildInputs = [
    (clojure.override { jdk = openjdk17; })
    glibcLocales # For rlwrap locale
    rlwrap
  ];
}
