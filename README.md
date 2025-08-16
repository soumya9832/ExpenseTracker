
\# Expense Tracker App

## 📝 App Overview

This is a modern Expense Tracker application built using Jetpack Compose. The app allows users to log their daily expenses, view a categorized list, and analyze their spending habits with a weekly report featuring both a bar chart and a category-wise pie chart. It is designed with a clean and intuitive UI to provide a seamless user experience.

## 🤖 AI Usage Summary

I utilized an AI assistant to enhance the development process and ensure the project's quality. Specifically, I used the AI to:

-   Generate and refine **Jetpack Compose UI components**, such as the Bar Chart and Pie Chart, including complex drawing logic for lines and shapes.
-   Debug and troubleshoot UI layout issues, like ensuring proper spacing and alignment for cards and text labels.
-   Refine the **MVVM architecture** by seeking best practices for state management and data flow using `StateFlow` and `ViewModel`.

## ⚙️ Checklist of Features Implemented

-   [x] **Summary Cards**: Displaying total expenses and transaction count.
-   [x] **Date Picker**: Allows users to filter expenses by a specific date.
-   [x] **Data Persistence**: Uses a local database to store expense data.
-   [x] **Dynamic Filtering & Sorting**: Expenses can be grouped by category or time and sorted by amount.
-   [x] **Reports Screen**: Provides a comprehensive view of spending over the last 7 days.
-   [x] **Report Sharing & Export**: Allows the user to share report or export it as a PDF or CSV file.
-   [x] **Bar Chart**: Visualizes daily spending.
-   [x] **Pie Chart**: Breaks down spending by category.
-   [x] **Animated Dialog**: Provides visual feedback upon a successful expense addition.
-   [x] **Modular UI**: Reusable composable functions for consistent design (e.g., `InsightCard`, `ExpenseListItem`).
-   [x] **MVVM Architecture**: Follows a clean and modular architecture.

## 🗒️ Prompt Logs

**Prompt 1**:
> My Expense entity includes a receiptImagePath: String?. I need to handle image uploads from the phone's gallery, save the image to the app's internal storage, and then save the permanent path to the receiptImagePath field in my Room database.

**Response 1**:
> To handle image uploads, you'll use an ActivityResultLauncher to get the image URI. The key is to then copy the image from that temporary URI to a permanent location in your app's internal storage...

**Prompt 2**:
> I have an issue where my image preview is clipping the content and pushing the button below it. I want the entry card to be vertically scrollable so the image isn't clipped and the button remains in a consistent position. Please provide a solution and the code.

**Response 2**:
> You are absolutely correct. Making the entire screen vertically scrollable is the best approach. It's a standard and more flexible pattern for forms with variable content, like yours with the image preview...
**Prompt 3**:
> I need to implement a detailed ReportsScreen. This screen should feature a pie chart that breaks down spending by category, with each segment colored according to its category and labeled with the category name and amount. Below the chart, there should be a list of cards summarizing the categorical data.

**Response 3**:
> I'm ready to build the Category-wise Spending Pie Chart. The design you provided shows a pie chart with colored segments that correspond to the category and amount, along with a list of category data cards below it...

## 🔗 Links

**APK Download Link**:
https://drive.google.com/file/d/1JmECE73EfP4oO8ub6w4p_gq48JtyQTUN/view?usp=sharing

**Screenshots**:
https://drive.google.com/drive/folders/1TdfUbaXt3Q8Piajp5C0Ol7PcT5keZs1C?usp=sharing
