# Backends

This directory contains backend services and applications for various web applications.

## Projects

### YouTube MVP
A comprehensive multi-module Maven project for a YouTube MVP platform with microservices architecture.

**Location:** `youtube-mvp/`

## Architecture Documentation

### High-Level Design (HLD) Diagrams

Some projects include PlantUML diagrams that visualize the high-level architecture.

#### Prerequisites for PlantUML
- Java 17 or higher
- Graphviz (for diagram rendering)

#### Installing Graphviz
```bash
# macOS
brew install graphviz

# Ubuntu/Debian
sudo apt-get install graphviz

# Windows
# Download from https://graphviz.org/download/
```

#### Generating PlantUML Diagrams
```bash
# Navigate to the project directory containing the .puml file
cd <project-directory>

# Download PlantUML JAR (if not already present)
curl -L -o plantuml.jar https://github.com/plantuml/plantuml/releases/download/v1.2025.8/plantuml-1.2025.8.jar

# Generate PNG diagram
java -jar plantuml.jar -tpng <diagram-name>.puml

# Generate SVG diagram
java -jar plantuml.jar -tsvg <diagram-name>.puml

# Validate syntax only (no output files)
java -jar plantuml.jar -checkonly <diagram-name>.puml
```

**Note:** You may see a warning about "Times" font not being available. This is a cosmetic warning on macOS and doesn't affect the diagram generation or quality. The diagrams will render correctly using available fonts.

#### Example: YouTube MVP HLD Diagram
```bash
cd youtube-mvp
java -jar plantuml.jar -tpng hld.puml
java -jar plantuml.jar -tsvg hld.puml
```

This will generate:
- `High-Level Design: YouTube-scale platform on Azure (DDD microservices on AKS).png`
- `High-Level Design: YouTube-scale platform on Azure (DDD microservices on AKS).svg`

## Contributing

When working on backend projects:
1. Follow the established code style for each project
2. Write unit and integration tests
3. Update documentation as needed
4. Follow the microservices architecture principles
5. Ensure proper error handling and logging
6. Update HLD diagrams when making architectural changes

## License

This project is licensed under the MIT License - see the LICENSE file for details.
