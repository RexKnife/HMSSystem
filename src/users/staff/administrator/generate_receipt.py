import sys
import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
from email.mime.base import MIMEBase
from email import encoders
from fpdf import FPDF


def create_pdf(appointment_id, patient_name, patient_email, total_amount, payment_method, appointment_date, appointment_time, doctor_name, service_type):
    pdf = FPDF()
    pdf.add_page()

    # Title
    pdf.set_font("Arial", style="B", size=16)
    pdf.cell(200, 10, txt="Hospital Receipt", ln=True, align="C")
    pdf.ln(10)

    # Appointment and Patient Details
    pdf.set_font("Arial", size=12)
    pdf.cell(200, 10, txt=f"Appointment ID: {appointment_id}", ln=True)
    pdf.cell(200, 10, txt=f"Patient Name: {patient_name}", ln=True)
    pdf.cell(200, 10, txt=f"Patient Email: {patient_email}", ln=True)
    pdf.cell(200, 10, txt=f"Doctor Name: {doctor_name}", ln=True)
    pdf.cell(200, 10, txt=f"Service Type: {service_type}", ln=True)
    pdf.cell(200, 10, txt=f"Appointment Date: {appointment_date}", ln=True)
    pdf.cell(200, 10, txt=f"Appointment Time: {appointment_time}", ln=True)

    # Payment Details
    pdf.cell(200, 10, txt=f"Total Amount: ${total_amount}", ln=True)
    pdf.cell(200, 10, txt=f"Payment Method: {payment_method}", ln=True)

    # Thank You Note
    pdf.ln(10)
    pdf.set_font("Arial", style="I", size=12)
    pdf.multi_cell(0, 10, "Thank you for your visit! If you have any questions, please contact us at support@hospital.com.", align="C")

    # Save the PDF
    pdf_file = f"receipt_{appointment_id}.pdf"
    pdf.output(pdf_file)
    return pdf_file


def send_email(patient_email, pdf_file):

    subject = "HMS Appointment Receipt"
    body = "Please find your receipt attached."

    # Email setup
    msg = MIMEMultipart()
    msg["From"] = sender_email
    msg["To"] = patient_email
    msg["Subject"] = subject

    msg.attach(MIMEText(body, "plain"))

    # Attach PDF
    with open(pdf_file, "rb") as attachment:
        part = MIMEBase("application", "octet-stream")
        part.set_payload(attachment.read())
        encoders.encode_base64(part)
        part.add_header("Content-Disposition", f"attachment; filename={pdf_file}")
        msg.attach(part)

    # Send email
    with smtplib.SMTP("smtp.gmail.com", 587) as server:
        server.starttls()
        server.login(sender_email, sender_password)
        server.send_message(msg)


if __name__ == "__main__":
    # Read arguments
    appointment_id = sys.argv[1]
    patient_name = sys.argv[2]
    patient_email = sys.argv[3]
    total_amount = sys.argv[4]
    payment_method = sys.argv[5]
    appointment_date = sys.argv[6]
    appointment_time = sys.argv[7]
    doctor_name = sys.argv[8]
    service_type = sys.argv[9]

    # Generate PDF
    pdf_file = create_pdf(
        appointment_id,
        patient_name,
        patient_email,
        total_amount,
        payment_method,
        appointment_date,
        appointment_time,
        doctor_name,
        service_type,
    )

    # Send email
    send_email(patient_email, pdf_file)
    print("Receipt sent successfully!")
