import logging
import os
from flask import Flask, request, jsonify
from waitress import serve
from openai import OpenAI

client = OpenAI(api_key=os.getenv('OPENAI_THINGY'))
app = Flask(__name__)

info_handler = logging.FileHandler('/home/nikdi/noitOff/logs/recipe-api-std.log')
info_handler.setLevel(logging.INFO)
info_formatter = logging.Formatter('%(asctime)s - %(levelname)s - %(message)s')
info_handler.setFormatter(info_formatter)

error_handler = logging.FileHandler('/home/nikdi/noitOff/logs/recipe-api-err.log')
error_handler.setLevel(logging.ERROR)
error_formatter = logging.Formatter('%(asctime)s - %(levelname)s - %(message)s')
error_handler.setFormatter(error_formatter)

app.logger.setLevel(logging.INFO)
app.logger.addHandler(info_handler)
app.logger.addHandler(error_handler)

def generate_recipe(ingredients, servings, portion_size):
  prompt = f"Създай рецепта за {servings} порции, където всяка тежи {portion_size} грама със тези съставки: {', '.join(ingredients)}"
  completion = client.chat.completions.create(
    model='gpt-4o-mini-2024-07-18',
    messages=[
      {"role": "user", "content": prompt}
    ],
    temperature=0.65
  )

  recipe = completion.choices[0].message.content
  return recipe

@app.route("/generate", methods=["POST"])
def server_logic():
  try:
        data = request.json
        user_ip = request.headers.get('X-Real-IP', "didn't parse")
        app.logger.info(f"Received data: {data} from {user_ip}")
        
        ingredients = data.get("ingredients", [])
        servings = data.get("servings", 1)
        portion_size = data.get("portion_size", 200)

        recipe = generate_recipe(ingredients, servings, portion_size)

        response = jsonify({"markdown": recipe})
        response.headers["Content-Type"] = "application/json"
        return response

  except Exception as e:
        app.logger.error(f"Error: {str(e)}")
        return jsonify({"markdown": "Изпитваме затруднения, моля опитайте по-късно!"}), 500

if __name__ == "__main__":
  serve(app, host="192.168.73.34", port=6000)
