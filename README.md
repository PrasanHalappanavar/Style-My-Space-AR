🏠 Style My Space – AR Based Interior Design Android App

Style My Space is an Augmented Reality (AR) based Android application that allows users to visualize home décor and furniture in real-world space using their mobile camera. The app helps users preview how furniture items like sofas, tables, chairs, and lamps will look in their room before purchasing.

📱 Features
🔐 Authentication

User login & signup

Admin login for product management

Firebase Authentication

🛋️ Product Management (Admin)

Add new furniture products

Upload product images and 3D models

Store product details in Firebase Firestore

Cloudinary integration for image & 3D model storage

🏡 User Module

View list of available products

Search products by name or category

View product details (image, price)

🧠 AR Visualization

Place 3D furniture models in real-world space

Models fetched dynamically from Cloudinary URLs

Move, rotate, and remove placed objects

Real-time AR rendering using device camera

🛒 Cart & Purchase

Add products to cart

View cart items with total price

Buy Now option (future enhancement)

🧩 Tech Stack
Category	Technologies
Platform	Android
Language	Java
AR	Google ARCore / Sceneform
Backend	Firebase Firestore
Authentication	Firebase Auth
Media Storage	Cloudinary
IDE	Android Studio
Architecture	MVVM (partial)
📂 Project Modules

Authentication Module

Admin Panel

Product Listing & Search

Product Details

Cart Management

AR Model Placement Module

🔄 App Workflow

User logs in / signs up

Admin adds furniture products (image + 3D model)

Products are stored in Firestore with Cloudinary URLs

User browses products and selects one

User opens AR view

Selected furniture is placed in real environment

User can remove or change placed models

🖼️ Screenshots

![Screenshot_20260101-124212_Style My Space](https://github.com/user-attachments/assets/45d36439-f217-4d19-b399-9aa351054d54)
![Screenshot_20260101-124224_Style My Space](https://github.com/user-attachments/assets/e253afec-e1f9-47cb-8e39-6836713a5581)
![Screenshot_20260101-124628_Style My Space](https://github.com/user-attachments/assets/5ae1b822-cdcc-45c8-af83-82ee0c8219f4)
![Screenshot_20260101-124705_Style My Space](https://github.com/user-attachments/assets/04748f55-b9aa-4ea4-acc0-34d04334cbd3)
![Screenshot_20260101-124800_Style My Space](https://github.com/user-attachments/assets/fb64fd24-dcb9-4ca8-bf5e-64348108872e)
![Screenshot_20260101-124829_Style My Space](https://github.com/user-attachments/assets/13690d7b-fb3d-4683-9d66-2b6bba26a9d0)
![Screenshot_20260101-124856_Style My Space](https://github.com/user-attachments/assets/e980c8b7-2e35-4b97-a6c9-ccc0018a1dd2)



Prerequisites

Android Studio (latest version)

ARCore supported Android device

Firebase Project

Cloudinary Account

Steps

Clone the repository

git clone https://github.com/PrasanHalappanavar/Style-My-Space.git


Open project in Android Studio

Connect Firebase to the project

Add google-services.json

Configure Cloudinary credentials

Run the app on a real device (AR required)


🔐 Permissions Used

Camera (for AR)

Internet (Firebase & Cloudinary)

Storage (optional for file selection)


👨‍💻 Developed By

Prasan Halappanavar
Software Engineer | Android Developer
