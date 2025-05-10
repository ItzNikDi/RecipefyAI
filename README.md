# 🍳 RecipefyAI - Вашият sous-chef с изкуствен интелект!

### RecipefyAI е мобилно приложение, което използва изкуствен интелект, за да генерира персонализирани рецепти и съвети за готвене. Комбинира Android технологии с машинно обучение, за да предостави интуитивно и интелигентно изживяване за всеки готвач - от изгряващ готвач до мастършеф!

## 🧠 Основни функции

- 📸 Добавяне на съставки чрез камера или текст
- 🧑‍🍳 Генериране на рецепти с помощта на AI (Python backend)
- 📝 Запазване на рецепти в Markdown формат
- 🕒⏩ Редактиране на рецепти (в бъдеще)

## 🚀 Как да се сдобиете с него? 
Към момента не поддържаме build-ване от изходен код поради липсата на подходящ метод за споделяне на модела, а и debug.keystore-а е все още присъстваща в него част.

Това настрана, **винаги** можете да намерите най-новата версия в [releases](https://github.com/ItzNikDi/RecipefyAI/releases) секцията на проекта, или от по-долу.

<p align="center">
   <a href="https://github.com/ItzNikDi/RecipefyAI?tab=readme-ov-file#installation">
   <img width="200" height="200" src="images-folder/qrcode-app.png"/>
   </a>
</p>

## ⚙️ Технологии и библиотеки

<p align="center">
   <a href="https://github.com/ItzNikDi/RecipefyAI?tab=readme-ov-file#installation">
   <img width="500" height="600" src="https://skillicons.dev/icons?i=androidstudio,kotlin,gradle,tensorflow,py,md,sqlite,github,pytorch&theme=light&perline=3"/>
   </a>
</p>

### Android (Kotlin)

- [**LiteRT**](https://ai.google.dev/edge/litert) – библиотека за изпълнение на модели за изкуствен интелект
- [**Room**](https://developer.android.com/jetpack/androidx/releases/room) – локална база данни за съхраняване на рецепти и предпочитания
- [**Markwon**](https://noties.io/Markwon/) – визуализиране на рецепти в четим и красив формат
- [**Retrofit**](https://square.github.io/retrofit/) + [**Gson**](https://github.com/google/gson) – HTTP заявки и обработка на JSON
- [**Coil**](https://coil-kt.github.io/coil/) – модерна, станала стандарт библиотека за работа с изображения в Android

### Backend (Python)

- [**Flask + Waitress**](https://flask.palletsprojects.com/en/stable/deploying/waitress/) - прост, но напълно подходящ за целта WSGI сървър
- [**OpenAI API**](https://platform.openai.com/docs/overview) заявка за генериране на рецепти по получената информация
- **Поддържан от нас, разработчиците на приложението, без изпращане на информация към трети страни**
---


## Искате да помогнете или сте срещнали проблем? - Направете pull request, отворете issue или се свържете с нас на recipefyai@gmail.com
