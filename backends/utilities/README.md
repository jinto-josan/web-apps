# Backend Utilities

This directory contains utilities and tools for backend development across all projects.

## 📁 Structure

```
utilities/
├── clean-architecture/           # Clean Architecture setup utilities
│   ├── create-clean-architecture.sh    # Generalized Clean Architecture script
│   ├── README.md                       # Clean Architecture documentation
│   ├── CLEAN_ARCHITECTURE.md          # Original YouTube MVP documentation
│   ├── create-clean-architecture-youtube.sh      # YouTube MVP specific script
│   └── create-clean-architecture-youtube-fixed.sh # YouTube MVP fixed script
└── README.md                      # This file
```

## 🛠️ Available Utilities

### Clean Architecture Generator

**Location**: `utilities/clean-architecture/`

A comprehensive tool for setting up Clean Architecture folder structures in microservices projects.

**Features**:
- ✅ Generalized for any project structure
- ✅ Interactive and command-line modes
- ✅ Automatic package name generation
- ✅ Flyway migration folder setup
- ✅ Example files generation
- ✅ Comprehensive documentation

**Quick Start**:
```bash
cd utilities/clean-architecture
chmod +x create-clean-architecture.sh
./create-clean-architecture.sh [project-root] [base-package] [services...]
```

**Examples**:
```bash
# Interactive mode
./create-clean-architecture.sh

# Command line mode
./create-clean-architecture.sh . com.youtube identity-auth-service user-profile-service

# Different project
./create-clean-architecture.sh /path/to/project com.company service1 service2
```

## 📚 Documentation

Each utility includes comprehensive documentation:

- **`utilities/clean-architecture/README.md`** - Complete usage guide and examples
- **`utilities/clean-architecture/CLEAN_ARCHITECTURE.md`** - Detailed architecture principles

## 🎯 Use Cases

These utilities are designed for:

- **New Projects**: Setting up consistent architecture from scratch
- **Existing Projects**: Converting services to Clean Architecture
- **Team Onboarding**: Ensuring consistent structure across teams
- **Code Reviews**: Maintaining architectural consistency
- **Documentation**: Providing clear structure guidelines

## 🚀 Getting Started

1. **Choose your utility** from the available tools
2. **Read the documentation** in the utility's README
3. **Run the utility** following the provided examples
4. **Customize** the generated structure for your needs
5. **Share** with your team for consistency

## 🤝 Contributing

To add new utilities:

1. Create a new directory under `utilities/`
2. Include a comprehensive README.md
3. Add the utility to this main README
4. Test with different project structures
5. Document usage examples

## 📋 Guidelines

When creating utilities:

- **Generalization**: Make tools work with any project structure
- **Documentation**: Include comprehensive README files
- **Examples**: Provide clear usage examples
- **Error Handling**: Include proper error handling and validation
- **Testing**: Test with different scenarios and project types

## 🔧 Maintenance

- **Version Control**: Keep utilities in sync with project needs
- **Documentation**: Update documentation when adding features
- **Testing**: Test utilities with new project structures
- **Feedback**: Incorporate team feedback for improvements

This utilities directory ensures consistent, high-quality backend development practices across all projects.
